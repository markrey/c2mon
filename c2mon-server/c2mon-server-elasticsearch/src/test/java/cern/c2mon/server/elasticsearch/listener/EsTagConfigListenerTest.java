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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import cern.c2mon.pmanager.persistence.exception.IDBPersistenceException;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.elasticsearch.indexer.EsTagConfigIndexer;
import cern.c2mon.server.elasticsearch.structure.converter.EsTagConfigConverter;
import cern.c2mon.server.elasticsearch.structure.types.tag.EsTagConfig;
import cern.c2mon.server.test.CacheObjectCreation;
import cern.c2mon.shared.client.configuration.ConfigConstants.Action;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Szymon Halastra
 */
@RunWith(MockitoJUnitRunner.class)
public class EsTagConfigListenerTest {

  @InjectMocks
  private EsTagConfigListener esTagConfigListener;

  @Mock
  private EsTagConfigConverter esTagConfigConverter;

  @Mock
  private EsTagConfigIndexer esTagConfigIndexer;

  private Tag tag = CacheObjectCreation.createTestDataTag();
  private EsTagConfig esTagConfig = new EsTagConfig();

  @Before
  public void setup() {
    when(esTagConfigConverter.convert(eq(tag))).thenReturn(esTagConfig);
  }

  @Test
  public void testEsTagConfigSentToIndexer() throws IDBPersistenceException {
    esTagConfigListener.onConfigurationEvent(tag, Action.CREATE);
    verify(esTagConfigConverter).convert(eq(tag));
  }
}
