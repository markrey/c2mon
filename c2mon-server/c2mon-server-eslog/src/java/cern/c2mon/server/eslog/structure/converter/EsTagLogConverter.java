/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 *
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 *
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.server.eslog.structure.converter;

import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.cache.SubEquipmentCache;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.equipment.AbstractEquipment;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.eslog.structure.mappings.EsMapping.ValueType;
import cern.c2mon.server.eslog.structure.types.GsonSupplier;
import cern.c2mon.server.eslog.structure.types.tag.*;
import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.datatag.TagQualityStatus;
import cern.c2mon.shared.common.metadata.Metadata;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Converts the dataTags from the server to the ElasticSearch format {@link AbstractEsTag}.
 *
 * @author Alban Marguet.
 */
@Slf4j
@Component
public class EsTagLogConverter {

  /**
   * Default ID to return if nothing is found in cache.
   */
  private static final long DEFAULT_ID = -1;

  private final Gson gson = GsonSupplier.INSTANCE.get();

  private final ProcessCache processCache;
  private final EquipmentCache equipmentCache;
  private final SubEquipmentCache subEquipmentCache;

  @Autowired
  public EsTagLogConverter(final ProcessCache processCache,
                           final EquipmentCache equipmentCache,
                           final SubEquipmentCache subEquipmentCache) {
    this.processCache = processCache;
    this.equipmentCache = equipmentCache;
    this.subEquipmentCache = subEquipmentCache;
  }

  /**
   * Converts all the properties of a Tag to create a {@link AbstractEsTag} according to the dataType.
   *
   * @param tag Tag object in C2MON.
   * @return {@link AbstractEsTag}, ready to be logged to the ElasticSearch instance.
   */
  public AbstractEsTag convertToTagES(final Tag tag) {
    AbstractEsTag esTag = instantiateTagES(tag.getDataType());
    if (esTag == null) {
      return null;
    }

    esTag.setId(tag.getId());
    esTag.setName(tag.getName());
    esTag.setDataType(tag.getDataType().toLowerCase());

    esTag.setRawValue(tag.getValue());

    esTag.setTimestamp(tag.getTimestamp().getTime());

    setDaqTimestamp(tag, esTag);
    setServerTimestamp(tag, esTag);

    setUnit(tag, esTag);
    setQualityAnalysis(tag, esTag);

    esTag.setValueDescription(tag.getValueDescription());

    setMetadata(tag, esTag);

    log.trace("convertToTagES() - new esTagImpl: " + esTag.toString());
    return esTag;
  }

  /**
   * Instantiate the right Class of {@link AbstractEsTag according to the dataType: boolean, String or int.
   *
   * @param dataType of the Tag in C2MON.
   * @return {@link AbstractEsTag}: {@link EsTagString }, {@link EsTagNumeric } or {@link EsTagBoolean } for ElasticSearch.
   */
  public AbstractEsTag instantiateTagES(String dataType) {
    dataType = dataType.toLowerCase();

    if (ValueType.isBoolean(dataType)) {
      return new EsTagBoolean();
    } else if (ValueType.isString(dataType)) {
      return new EsTagString();
    } else if (ValueType.isNumeric(dataType)) {
      return new EsTagNumeric();
    } else {
      log.warn("instantiateTagES() - Tag did not correspond to any existing type. (dataType = " + dataType + ").");
      return null;
    }
  }

  private void setDaqTimestamp(Tag tag, AbstractEsTag esTag) {
    if (!(tag instanceof DataTag)) {
      return;
    }
    Optional.ofNullable(((DataTag) tag).getDaqTimestamp())
            .map(Timestamp::getTime)
            .ifPresent(esTag::setDaqTimestamp);
  }

  private void setServerTimestamp(Tag tag, AbstractEsTag esTag) {
    Optional.ofNullable(tag.getCacheTimestamp())
            .map(Timestamp::getTime)
            .ifPresent(esTag::setServerTimestamp);
  }

  private void setUnit(Tag tag, AbstractEsTag esTag) {
    Optional.ofNullable(tag.getUnit())
        .filter(StringUtils::isNotBlank)
        .ifPresent(esTag::setUnit);
  }

  private void setQualityAnalysis(final Tag tag, final AbstractEsTag esTag) {
    final DataTagQuality dataTagQuality = tag.getDataTagQuality();
    if(dataTagQuality == null) {
      return;
    }

    final TagQualityAnalysis qualityAnalysis = new TagQualityAnalysis();

    qualityAnalysis.setValid(dataTagQuality.isValid());

    qualityAnalysis.setStatus(calculateStatus(tag));
    qualityAnalysis.setStatusInfo(collectStatusInfo(dataTagQuality));

    esTag.setQuality(qualityAnalysis);
  }

  /**
   * Calculates the accumulated status of a {@link Tag},
   * based on the individual quality statuses that it contains.
   *
   * @param tag the {@link Tag} instance that is used to extract the general status
   * @return the result of the accumulated statuses.
   *         If no invalid status was found (good quality) the result will be {@code 0}.
   *
   */
  private int calculateStatus(final Tag tag) {
    return Optional.ofNullable(tag.getDataTagQuality())
        .map(dataTagQuality -> dataTagQuality.getInvalidQualityStates().keySet())
        .map(tagQualityStatuses -> tagQualityStatuses.stream()
            .mapToInt(TagQualityStatus::getCode)
            .map(statusCode -> (int) Math.pow(2, statusCode))
            .sum())
        .orElse(0);
  }

  /**
   * Collects the individual statuses with their description, if any,
   * based on a {@link DataTagQuality} instance.
   *
   * @param dataTagQuality the tag quality instance, that contains
   *                       the individual quality information
   * @return a {@link Collection} of invalid statuses.
   *         a {@link Collection} with a single value {@code "OK"}, if no invalid qualities were found.
   *
   */
  private Collection<String> collectStatusInfo(final DataTagQuality dataTagQuality) {
    Collection<String> invalidQualityInfo = Optional.ofNullable(dataTagQuality.getInvalidQualityStates())
        .map(Map::entrySet)
        .map(Collection::stream)
        .orElse(Stream.empty())
        .map(invalidQualityState -> invalidQualityState.getKey().name() + " : " + invalidQualityState.getValue())
        .collect(Collectors.toSet());

    if(invalidQualityInfo.isEmpty()) {
      return Collections.singleton(TagQualityAnalysis.OK);
    }

    return invalidQualityInfo;
  }

  /**
   * Handles Metadata of tag and retrieve process, equipment and subEquipment.
   */
  private void setMetadata(final Tag tag, final AbstractEsTag esTag) {
    esTag.getMetadata().putAll(retrieveTagProcessMetadata(tag));
    esTag.getMetadata().putAll(retrieveTagMetadata(tag));
  }

  private Map<String, String> retrieveTagMetadata(Tag tag) {
    Metadata metadata = tag.getMetadata();
    if (metadata == null) {
      return Collections.emptyMap();
    }
    return metadata.getMetadata().entrySet().stream()
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            entry -> entry.getValue().toString()));
  }

  /**
   * Retrieve the ProcessName [, EquipmentName, SubEquipmentName] for a given
   * Tag. According to the hierarchy.
   *
   * @param tag for which to get the metadata
   * @return List of names in the order ProcessName [, EquipmentName,
   * SubEquipmentName].
   */
  public Map<String, String> retrieveTagProcessMetadata(Tag tag) {

    long equipmentId = DEFAULT_ID;
    long subEquipmentId = DEFAULT_ID;
    long processId = DEFAULT_ID;

    boolean subEquipmentIsPresent = !CollectionUtils.isEmpty(tag.getSubEquipmentIds());
    boolean EquipmentIsPresent = !CollectionUtils.isEmpty(tag.getEquipmentIds());
    boolean processIsPresent = !CollectionUtils.isEmpty(tag.getProcessIds());

    if (subEquipmentIsPresent) {
      subEquipmentId = tag.getSubEquipmentIds().iterator().next();
      equipmentId = searchEquipmentInSubEquipmentCache(subEquipmentId);
      processId = searchProcessIdInEquipmentCache(equipmentId);

      return extractMetadata(processId, equipmentId, subEquipmentId);
    }

    if (EquipmentIsPresent) {
      equipmentId = tag.getEquipmentIds().iterator().next();
      processId = searchProcessIdInEquipmentCache(equipmentId);

      return extractMetadata(processId, equipmentId, subEquipmentId);
    }

    if (processIsPresent) {
      processId = tag.getProcessIds().iterator().next();

      return extractMetadata(processId, equipmentId, subEquipmentId);
    }

    log.info("no Process, Equipment or subEquipment");
    return Collections.emptyMap();
  }

  private Map<String, String> extractMetadata(long processId, long equipmentId, long subEquipmentId) {
    final Map<String, String> metadata = new HashMap<>();

    extractProcessName(processId)
        .ifPresent(processName -> metadata.put("process", processName));
    extractEquipmentName(equipmentId)
        .ifPresent(equipmentName -> metadata.put("equipment", equipmentName));
    extractSubEquipmentName(subEquipmentId)
        .ifPresent(subEquipmentName -> metadata.put("subEquipment", subEquipmentName));

    return metadata;
  }

  private long searchEquipmentInSubEquipmentCache(long subEquipmentId) {
    return Optional.ofNullable(subEquipmentCache.get(subEquipmentId))
            .map(SubEquipment::getParentId)
            .orElse(DEFAULT_ID);
  }

  private long searchProcessIdInEquipmentCache(long equipmentId) {
    return Optional.ofNullable(equipmentCache.get(equipmentId))
            .map(Equipment::getProcessId)
            .orElse(DEFAULT_ID);
  }

  private Optional<String> extractProcessName(long processId) {
    return Optional.ofNullable(processCache.get(processId))
            .map(Process::getName);
  }

  private Optional<String> extractEquipmentName(long equipmentId) {
    if (equipmentId == -1) {
      return Optional.empty();
    }
    return Optional.ofNullable(equipmentCache.get(equipmentId))
            .map(AbstractEquipment::getName);
  }

  private Optional<String> extractSubEquipmentName(long subEquipmentId) {
    if (subEquipmentId == -1) {
      return Optional.empty();
    }
    return Optional.ofNullable(subEquipmentCache.get(subEquipmentId))
            .map(AbstractEquipment::getName);
  }
}