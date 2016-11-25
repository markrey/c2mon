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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.pmanager.IDBPersistenceHandler;
import cern.c2mon.server.cache.CacheRegistrationService;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.elasticsearch.structure.converter.EsTagConfigConverter;
import cern.c2mon.server.elasticsearch.structure.types.tag.EsTagConfig;
import cern.c2mon.server.test.CacheObjectCreation;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

/**
 * @author Szymon Halastra
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
        EsTagConfigListenerTest.TagLogListenerTestConfiguration.class
})
public class EsTagConfigListenerTest {

  @Configuration
  public static class TagLogListenerTestConfiguration {
    @Bean
    public EsTagConfigConverter esTagLogConverter() {
      return mock(EsTagConfigConverter.class);
    }

    @Bean
    public IDBPersistenceHandler<EsTagConfig> esTagConfigIndexer() {
      return mock(IDBPersistenceHandler.class);
    }

    @Bean
    public EsTagConfigListener esTagLogListener() {
      return new EsTagConfigListener(
              esTagConfigIndexer(),
              esTagLogConverter());
    }
  }

  @Before
  public void setUp() throws Exception {
    reset();
  }

  @Autowired
  private EsTagConfigConverter esLogConverter;

  @Autowired
  private CacheRegistrationService cacheRegistrationService;

  @Autowired
  private EsTagConfigListener esTagConfigListener;

  @Test
  public void test() {
    DataTagCacheObject tag = CacheObjectCreation.createTestDataTag();
  }
}
