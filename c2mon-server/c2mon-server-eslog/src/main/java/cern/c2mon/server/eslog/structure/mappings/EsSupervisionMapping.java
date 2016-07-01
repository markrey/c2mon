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
package cern.c2mon.server.eslog.structure.mappings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * EsMapping for {@link cern.c2mon.server.eslog.structure.types.EsSupervisionEvent} in ElasticSearch.
 *
 * @author Alban Marguet
 */
@Slf4j
@Data
public class EsSupervisionMapping implements EsMapping {
  private SupervisionProperties mappings;
  private static transient Gson gson = new GsonBuilder().setPrettyPrinting().create();

  /**
   * Instantiate a new EsSupervisionMapping by setting it Properties to supervision type.
   */
  public EsSupervisionMapping() {
    mappings = new SupervisionProperties();
  }

  /**
   * @return the mapping as JSON String for ElasticSearch.
   */
  public String getMapping() {
    String json = gson.toJson(this);
    log.trace("getMapping() - Created the supervision mapping: " + json);
    return json;
  }
}