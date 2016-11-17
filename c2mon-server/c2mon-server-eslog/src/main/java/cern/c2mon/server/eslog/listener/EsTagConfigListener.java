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

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;

import cern.c2mon.pmanager.persistence.IPersistenceManager;
import cern.c2mon.server.cache.C2monCacheListener;
import cern.c2mon.server.common.component.Lifecycle;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.eslog.structure.converter.EsTagConfigConverter;
import cern.c2mon.server.eslog.structure.types.tag.EsTagConfig;

/**
 * @author Szymon Halastra
 */
@Data
@Slf4j
@Service
public class EsTagConfigListener implements C2monCacheListener<Tag>, SmartLifecycle {

  private final IPersistenceManager<EsTagConfig> esTagConfigPersistenceManager;
  /**
   * Listener container lifecycle hook.
   */
  private Lifecycle listenerContainer;

  EsTagConfigConverter configConverter;

  /**
   * Lifecycle flag.
   */
  private volatile boolean running = false;

  @Autowired
  public EsTagConfigListener(@Qualifier("esTagConfigPersistenceManager") final IPersistenceManager<EsTagConfig> esTagConfigPersistenceManager,
                             final EsTagConfigConverter configConverter) {
    this.esTagConfigPersistenceManager = esTagConfigPersistenceManager;
    this.configConverter = configConverter;
  }


  @Override
  public boolean isAutoStartup() {
    return false;
  }

  @Override
  public void stop(Runnable runnable) {

  }

  @Override
  public void start() {

  }

  @Override
  public void stop() {

  }

  @Override
  public boolean isRunning() {
    return false;
  }

  @Override
  public int getPhase() {
    return 0;
  }

  @Override
  public void notifyElementUpdated(Tag cacheable) {
    esTagConfigPersistenceManager.storeData(configConverter.convert(cacheable));

  }

  @Override
  public void confirmStatus(Tag cacheable) {

  }
}
