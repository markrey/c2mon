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

import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cern.c2mon.pmanager.persistence.exception.IDBPersistenceException;
import cern.c2mon.server.elasticsearch.config.BaseElasticsearchIntegrationTest;
import cern.c2mon.server.elasticsearch.config.ElasticsearchProperties;
import cern.c2mon.server.elasticsearch.connector.TransportConnector;
import cern.c2mon.server.elasticsearch.structure.types.tag.EsTagConfig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Szymon Halastra
 */
public class EsTagConfigIndexerTest extends BaseElasticsearchIntegrationTest {

  private static final String CONF_TAG_INDEX = "c2mon-conf-tag";

  @Autowired
  private EsTagConfigIndexer<EsTagConfig> indexer;

  @Autowired
  TransportConnector connector;

  @Autowired
  private ElasticsearchProperties properties;

  @Test
  public void testInit() {
    assertTrue(connector.isConnected());
  }

  @Test
  public void testStoreSingleEsTagConfig() throws IDBPersistenceException {
    EsTagConfig esTagConfig = new EsTagConfig(1L, Boolean.class.getName());

    connector.getClient().admin().indices().delete(new DeleteIndexRequest(CONF_TAG_INDEX)).actionGet();

    indexer.storeData(esTagConfig);

    SearchResponse response = connector.getClient().prepareSearch(new String[]{CONF_TAG_INDEX})
            .setSearchType(SearchType.DEFAULT).setQuery(QueryBuilders.matchAllQuery())
            .execute().actionGet();

    assertEquals(1, response.getHits().getTotalHits());
  }
}
