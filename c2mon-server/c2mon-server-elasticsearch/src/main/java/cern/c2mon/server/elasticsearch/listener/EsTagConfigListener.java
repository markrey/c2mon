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

package cern.c2mon.server.elasticsearch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import cern.c2mon.server.common.listener.ConfigurationEventListener;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.elasticsearch.indexer.EsTagConfigIndexer;
import cern.c2mon.server.elasticsearch.structure.converter.EsTagConfigConverter;
import cern.c2mon.shared.client.configuration.ConfigConstants.Action;

/**
 * @author Szymon Halastra
 */
@Slf4j
@Component
public class EsTagConfigListener implements ConfigurationEventListener {

  private final EsTagConfigIndexer esTagConfigIndexer;

  private final EsTagConfigConverter esTagConfigConverter;

  @Autowired
  public EsTagConfigListener(@Qualifier("esTagConfigIndexer") final EsTagConfigIndexer esTagConfigIndexer,
                             final EsTagConfigConverter esTagConfigConverter) {
    this.esTagConfigIndexer = esTagConfigIndexer;
    this.esTagConfigConverter = esTagConfigConverter;
  }

  @Override
  public void onConfigurationEvent(Tag tag, Action action) {
    try {

      switch (action) {
        case CREATE:
          esTagConfigIndexer.indexTagConfig(esTagConfigConverter.convert(tag));
          break;
        case UPDATE:
          esTagConfigIndexer.updateTagConfig(esTagConfigConverter.convert(tag));
          break;
        case REMOVE:
          esTagConfigIndexer.removeTagConfig(esTagConfigConverter.convert(tag));
          break;
        default:
          break;
      }

    }
    catch (Exception e) {
      throw new RuntimeException("Error indexing tag configuration", e);
    }
  }
}