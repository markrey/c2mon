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

import lombok.Data;

import cern.c2mon.server.common.tag.Tag;

@Data
public class EsTagC2monInfo extends C2monInfo {

  /**
   * The fully qualified value (classname) of a tag's
   * enclosed metric value
   */
  protected final String dataType;

  /**
   * The time when the server received the {@link Tag}
   */
  private long serverTimestamp;

  /**
   * The time when the {@link Tag} value was collected.
   */
  private long sourceTimestamp;

  /**
   * The time when the DAQ received the {@link Tag}
   */
  private long daqTimestamp;
}
