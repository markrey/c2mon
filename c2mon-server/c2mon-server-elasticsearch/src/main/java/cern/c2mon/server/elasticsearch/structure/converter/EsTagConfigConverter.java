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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.elasticsearch.structure.types.tag.EsTagConfig;

/**
 * @author Szymon Halastra
 */
@Component
@Slf4j
public class EsTagConfigConverter implements Converter<Tag, EsTagConfig> {

  @Override
  public EsTagConfig convert(Tag tag) {
    EsTagConfig esTagConfig = new EsTagConfig(tag.getId(), tag.getName(), tag.getDataType(), retrieveTagMetadata(tag));

    return esTagConfig;
  }

  private Map<String, String> retrieveTagMetadata(Tag tag) {
    if (tag.getMetadata() == null) {
      return Collections.emptyMap();
    }

    Map<String, String> metadata = new HashMap<>();
    tag.getMetadata().getMetadata().forEach((k, v) -> metadata.put(k, v == null ? null : v.toString()));
    return metadata;
  }
}