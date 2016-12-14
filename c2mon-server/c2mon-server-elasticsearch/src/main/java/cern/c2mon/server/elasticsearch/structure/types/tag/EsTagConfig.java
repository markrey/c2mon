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

package cern.c2mon.server.elasticsearch.structure.types.tag;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import cern.c2mon.server.elasticsearch.structure.types.GsonSupplier;
import cern.c2mon.shared.client.tag.TagMode;

/**
 * @author Szymon Halastra
 */
@Data
@Slf4j
public class EsTagConfig {

  @NonNull
  protected static final transient Gson gson = GsonSupplier.INSTANCE.get();

  private long id;
  private String name;
  private String unit;
  private String description;
  private TagMode mode;
  private Date timestamp;


  //TODO: add min max values, check DatatagCacheObject, define as double
//  private Comparable maxValue = null;
//
//  private Comparable minValue = null;

  private Map<String, String> metadata = new HashMap<>();
  private EsTagConfigC2monInfo c2mon;

  public EsTagConfig(Long id, String name, String unit, String description,
                     short mode, Date timestamp, String dataType, Map<String, String> metadata) {
    this.id = id;
    this.name = name;
    this.unit = unit;
    this.description = description;
    this.mode = TagMode.values()[mode];
    this.timestamp = timestamp;
    this.c2mon = new EsTagConfigC2monInfo(dataType);
    this.metadata = metadata;
  }

  @Override
  public String toString() {
    String json = gson.toJson(this);
    log.debug(json);
    return json;
  }
}
