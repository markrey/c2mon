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

package cern.c2mon.server.eslog.listener;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.pmanager.persistence.IPersistenceManager;
import cern.c2mon.server.cache.CacheRegistrationService;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.eslog.structure.converter.EsTagConfigConverter;
import cern.c2mon.server.eslog.structure.types.tag.EsTagConfig;
import cern.c2mon.server.test.CacheObjectCreation;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author Szymon Halastra
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = EsTagConfigListenerTest.TagLogListenerTestConfiguration.class)
public class EsTagConfigListenerTest {

  @Configuration
  public static class TagLogListenerTestConfiguration {
    @Bean
    public EsTagConfigConverter esTagLogConverter() {
      return mock(EsTagConfigConverter.class);
    }

    @Bean
    public IPersistenceManager<EsTagConfig> esTagConfigPersistenceManager() {
      return mock(IPersistenceManager.class);
    }

    @Bean
    public CacheRegistrationService cacheRegistrationService() {
      return mock(CacheRegistrationService.class);
    }

    @Bean
    public EsTagConfigListener esTagLogListener() {
      return new EsTagConfigListener(
              esTagConfigPersistenceManager(),
              esTagLogConverter(),
              cacheRegistrationService());
    }
  }

  @Before
  public void setUp() throws Exception {
    reset(esLogConverter,
            cacheRegistrationService,
            esTagConfigPersistenceManager);
  }

  @Autowired
  private EsTagConfigConverter esLogConverter;

  @Autowired
  private CacheRegistrationService cacheRegistrationService;


  @Autowired
  @Qualifier("esTagConfigPersistenceManager")
  private IPersistenceManager<EsTagConfig> esTagConfigPersistenceManager;


  @Autowired
  private EsTagConfigListener esTagConfigListener;

  @Test
  public void test() {
    DataTagCacheObject tag = CacheObjectCreation.createTestDataTag();
    esTagConfigListener.notifyElementUpdated(Collections.<Tag>singletonList(tag));
    verify(esLogConverter).convert(eq(tag));
  }
}
