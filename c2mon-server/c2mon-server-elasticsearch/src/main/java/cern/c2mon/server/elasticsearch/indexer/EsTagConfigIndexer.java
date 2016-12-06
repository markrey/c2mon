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

package cern.c2mon.server.elasticsearch.indexer;

import javax.annotation.PostConstruct;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cern.c2mon.server.elasticsearch.config.ElasticsearchProperties;
import cern.c2mon.server.elasticsearch.connector.TransportConnector;
import cern.c2mon.server.elasticsearch.structure.mappings.EsTagConfigMapping;
import cern.c2mon.server.elasticsearch.structure.types.tag.EsTagConfig;

/**
 * @author Szymon Halastra
 */
@Slf4j
@Component
public class EsTagConfigIndexer {

  private static final String TYPE = "tag_config";
  @Autowired
  @Setter
  private ElasticsearchProperties properties;

  @Autowired
  private TransportConnector connector;

  @Autowired
  public EsTagConfigIndexer(final TransportConnector connector, final ElasticsearchProperties properties) {
    this.connector = connector;
    this.properties = properties;
  }

  @PostConstruct
  public void init() {
    connector.createIndex(properties.getTagConfigIndex());
    connector.createIndexTypeMapping(properties.getTagConfigIndex(), TYPE,
            new EsTagConfigMapping().getMapping());
  }

  public void indexTagConfig(EsTagConfig tag) {
    IndexRequest indexNewTag = new IndexRequest(properties.getTagConfigIndex(), TYPE,
            String.valueOf(tag.getId())).source(tag.toString());

    try {
      connector.getClient().index(indexNewTag).get();
      connector.waitForYellowStatus();
    }
    catch (Exception e) {
      log.error("Error occurred while indexing the config for tag #{}", tag.getId(), e);
    }
  }

  public void updateTagConfig(EsTagConfig tag) {
    UpdateRequest updateRequest = new UpdateRequest(properties.getTagConfigIndex(), TYPE,
            String.valueOf(tag.getId())).doc(tag.toString());

    try {
      connector.getClient().update(updateRequest).get();
      connector.waitForYellowStatus();
    }
    catch (Exception e) {
      log.error("Error occurred while updating the config for tag #{}", tag.getId(), e);
    }
  }

  public void removeTagConfig(EsTagConfig tag) {
    DeleteRequest deleteRequest = new DeleteRequest(properties.getTagConfigIndex(), TYPE,
            String.valueOf(tag.getId()));

    try {
      connector.getClient().delete(deleteRequest).get();
      connector.waitForYellowStatus();
    }
    catch (Exception e) {
      log.error("Error occurred while deleting the config for tag #{}", tag.getId(), e);
    }
  }
}
