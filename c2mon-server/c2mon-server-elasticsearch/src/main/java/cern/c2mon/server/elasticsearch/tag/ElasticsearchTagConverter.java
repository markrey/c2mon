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
package cern.c2mon.server.elasticsearch.tag;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.cache.SubEquipmentCache;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.tag.Tag;

import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.datatag.TagQualityStatus;

/**
 * Converts {@link DataTag} instances to {@link EsTag} instances.
 *
 * @author Alban Marguet
 */
@Slf4j
@Component
public class ElasticsearchTagConverter extends TagConverter implements Converter<Tag, EsTag> {

  @Autowired
  public ElasticsearchTagConverter(final ProcessCache processCache,
                                   final EquipmentCache equipmentCache,
                                   final SubEquipmentCache subEquipmentCache) {
    super(processCache, equipmentCache, subEquipmentCache);
  }

  /**
   * Converts all the properties of a Tag to create a {@link EsTag} according to the dataType.
   *
   * @param tag Tag object in C2MON.
   * @return {@link EsTag}, ready to be logged to the Elasticsearch instance.
   */
  @Override
  public EsTag convert(final Tag tag) {
    EsTag esTag = new EsTag(tag.getId(), tag.getDataType());

    esTag.setName(tag.getName());
    esTag.setRawValue(tag.getValue());
    esTag.setValueDescription(tag.getValueDescription());

    setUnit(tag, esTag);
    setQualityAnalysis(tag, esTag);

    esTag.setTimestamp(tag.getTimestamp().getTime());

    esTag.setC2mon(extractC2MonInfo(tag, esTag.getC2mon()));
    esTag.getMetadata().putAll(retrieveTagMetadata(tag));

    log.trace("convert() - new esTagImpl: " + esTag.toString());
    return esTag;
  }

  /**
   * Calculates the accumulated status of a {@link Tag},
   * based on the individual quality statuses that it contains.
   *
   * @param tag the {@link Tag} instance that is used to extract the general status
   * @return the result of the accumulated statuses.
   *         If no invalid status was found (good quality) the result will be {@code 0}.
   */
  private int calculateStatus(final Tag tag) {
    return Optional.ofNullable(tag.getDataTagQuality())
            .map(DataTagQuality::getInvalidQualityStates)
            .map(Map::keySet)
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
    final String delimiter = " : ";

    Map<TagQualityStatus, String> invalidQualityStates = dataTagQuality.getInvalidQualityStates();
    if(invalidQualityStates == null) {
      return Collections.singleton(TagQualityAnalysis.OK);
    }

    Collection<String> invalidQualityInfo = invalidQualityStates.entrySet().stream()
            .map(invalidQualityState -> String.format("%s %s %s",
                    invalidQualityState.getKey().name(), delimiter, invalidQualityState.getValue()))
            .collect(Collectors.toSet());

    if(invalidQualityInfo.isEmpty()) {
      return Collections.singleton(TagQualityAnalysis.OK);
    }

    return invalidQualityInfo;
  }

  private void setUnit(Tag tag, EsTag esTag) {
    String unit = Optional.ofNullable(tag.getUnit())
            .filter(StringUtils::isNotBlank)
            .orElse("");
    esTag.setUnit(unit);
  }

  private void setQualityAnalysis(final Tag tag, final EsTag esTag) {
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

  protected EsTagC2monInfo extractC2MonInfo(final Tag tag, final EsTagC2monInfo c2monInfo) {
    final Map<String, String> tagProcessMetadata = retrieveTagProcessMetadata(tag);

    c2monInfo.setProcess(tagProcessMetadata.get("process"));
    c2monInfo.setEquipment(tagProcessMetadata.get("equipment"));
    c2monInfo.setSubEquipment(tagProcessMetadata.get("subEquipment"));

    setServerTimestamp(tag, c2monInfo);
    setSourceTimeStamp(tag, c2monInfo);
    setDaqTimestamp(tag, c2monInfo);

    return c2monInfo;
  }

  private void setServerTimestamp(Tag tag, EsTagC2monInfo c2MonInfo) {
    Optional.ofNullable(tag.getCacheTimestamp())
            .map(Timestamp::getTime)
            .ifPresent(c2MonInfo::setServerTimestamp);
  }

  private void setSourceTimeStamp(Tag tag, EsTagC2monInfo c2MonInfo) {
    if (!(tag instanceof DataTag)) {
      return;
    }
    Optional.ofNullable(((DataTag) tag).getSourceTimestamp())
            .map(Timestamp::getTime)
            .ifPresent(c2MonInfo::setSourceTimestamp);
  }

  private void setDaqTimestamp(Tag tag, EsTagC2monInfo c2MonInfo) {
    if (!(tag instanceof DataTag)) {
      return;
    }
    Optional.ofNullable(((DataTag) tag).getDaqTimestamp())
            .map(Timestamp::getTime)
            .ifPresent(c2MonInfo::setDaqTimestamp);
  }

}
