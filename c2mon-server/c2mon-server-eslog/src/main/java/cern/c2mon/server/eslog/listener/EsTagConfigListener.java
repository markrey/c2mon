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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import cern.c2mon.pmanager.persistence.IPersistenceManager;
import cern.c2mon.server.cache.C2monBufferedCacheListener;
import cern.c2mon.server.cache.CacheRegistrationService;
import cern.c2mon.server.common.component.Lifecycle;
import cern.c2mon.server.common.config.ServerConstants;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.eslog.structure.converter.EsTagConfigConverter;
import cern.c2mon.server.eslog.structure.types.tag.EsTagConfig;

/**
 * @author Szymon Halastra
 */
@Data
@Slf4j
@Service
public class EsTagConfigListener implements C2monBufferedCacheListener<Tag>, SmartLifecycle {

  private final IPersistenceManager<EsTagConfig> esTagConfigPersistenceManager;

  /**
   * Reference to registration service.
   */
  private final CacheRegistrationService cacheRegistrationService;

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
                             final EsTagConfigConverter configConverter,
                             final CacheRegistrationService cacheRegistrationService) {
    this.esTagConfigPersistenceManager = esTagConfigPersistenceManager;
    this.configConverter = configConverter;
    this.cacheRegistrationService = cacheRegistrationService;

    log.info("ESTagConfigListener is running");
  }

  /**
   * Registers to be notified of all Tag updates (data, rule and control tags).
   */
  @PostConstruct
  public void init() {
    listenerContainer = cacheRegistrationService.registerBufferedListenerToTags(this);
  }


  @Override
  public boolean isAutoStartup() {
    return false;
  }

  @Override
  public void stop(Runnable runnable) {
    stop();
    runnable.run();
  }

  @Override
  public void start() {
    log.debug("Starting Tag logger (esTagConfig");
    running = true;
    listenerContainer.start();
  }

  @Override
  public void stop() {
    log.debug("Stopping Tag logger (esTagConfig)");
    running = true;
    listenerContainer.start();
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  @Override
  public int getPhase() {
    return ServerConstants.PHASE_STOP_LAST - 1;
  }

  @Override
  public void notifyElementUpdated(Collection<Tag> collection) {
    if (collection == null) {
      log.warn("notifyElementUpdated() = Received a null collection of tags");
      return;
    }
    log.info("notifyElementUpdated() - Received a collection of " + collection.size() + " elements");

    esTagConfigPersistenceManager.storeData(convertTagsToEsTags(collection));
  }

  @Override
  public void confirmStatus(Collection<Tag> eventCollection) {

  }

  private List<EsTagConfig> convertTagsToEsTags(final Collection<Tag> tagsToLog) {
    final List<EsTagConfig> esTagList = new ArrayList<>();
    if (CollectionUtils.isEmpty(tagsToLog)) {
      return esTagList;
    }

    tagsToLog.forEach(tag -> esTagList.add(configConverter.convert(tag)));

    return esTagList;
  }
}
