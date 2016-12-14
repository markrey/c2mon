/*******************************************************************************
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
 ******************************************************************************/

package cern.c2mon.server.elasticsearch.structure.converter;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.cache.SubEquipmentCache;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.equipment.AbstractEquipment;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.elasticsearch.structure.types.tag.EsTagC2monInfo;
import cern.c2mon.server.elasticsearch.structure.types.tag.EsTagConfigC2monInfo;

/**
 * @author Szymon Halastra
 */
@Slf4j
public abstract class TagConverter {

  protected final ProcessCache processCache;
  protected final EquipmentCache equipmentCache;
  protected final SubEquipmentCache subEquipmentCache;

  public TagConverter(final ProcessCache processCache,
               final EquipmentCache equipmentCache,
               final SubEquipmentCache subEquipmentCache) {
    this.processCache = processCache;
    this.equipmentCache = equipmentCache;
    this.subEquipmentCache = subEquipmentCache;
  }

  /**
   * Default ID to return if nothing is found in cache.
   */
  protected static final long DEFAULT_ID = -1;

  protected Map<String, String> retrieveTagMetadata(Tag tag) {
    if (tag.getMetadata() == null) {
      return Collections.emptyMap();
    }

    Map<String, String> metadata = new HashMap<>();
    tag.getMetadata().getMetadata().forEach((k, v) -> metadata.put(k, v == null ? null : v.toString()));
    return metadata;
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

    log.debug("no Process, Equipment or SubEquipment found for tag #{}", tag.getId());
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
