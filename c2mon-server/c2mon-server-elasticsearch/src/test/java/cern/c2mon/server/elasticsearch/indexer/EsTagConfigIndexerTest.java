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

import org.elasticsearch.action.index.IndexRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import cern.c2mon.pmanager.persistence.exception.IDBPersistenceException;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.elasticsearch.connector.TransportConnector;
import cern.c2mon.server.elasticsearch.structure.converter.EsTagConfigConverter;
import cern.c2mon.server.elasticsearch.structure.mappings.EsTagConfigMapping;
import cern.c2mon.server.elasticsearch.structure.types.tag.EsTagConfig;
import cern.c2mon.server.test.CacheObjectCreation;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * @author Szymon Halastra
 */
@RunWith(MockitoJUnitRunner.class)
public class EsTagConfigIndexerTest {
  private Tag tag;
  private EsTagConfig esTagConfig;
  private EsTagConfigMapping esTagConfigMapping;

  @InjectMocks
  private EsTagConfigIndexer<EsTagConfig> indexer;

  @Mock
  private TransportConnector connector;
  private EsTagConfigConverter converter = new EsTagConfigConverter();

  @Before
  public void setup() throws IDBPersistenceException {
    tag = CacheObjectCreation.createTestDataTag();
    esTagConfig = converter.convert(tag);

//    when(connector.logTagConfig(new IndexRequest(anyString(), anyString(), eq(esTagConfig.toString())))).thenReturn(true);
  }

  @Test
  public void testInitWell() throws IDBPersistenceException {
    when(connector.isConnected()).thenReturn(true);
    indexer.init();
    assertTrue(indexer.isAvailable());
  }
}
