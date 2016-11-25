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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import cern.c2mon.pmanager.IDBPersistenceHandler;
import cern.c2mon.pmanager.persistence.exception.IDBPersistenceException;
import cern.c2mon.server.cache.C2monBufferedCacheListener;
import cern.c2mon.server.common.component.Lifecycle;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.elasticsearch.structure.converter.EsTagConfigConverter;
import cern.c2mon.server.elasticsearch.structure.types.tag.EsTagConfig;

/**
 * @author Szymon Halastra
 */
@Data
@Slf4j
@Service
public class EsTagConfigListener implements C2monBufferedCacheListener<Tag>, SmartLifecycle {

  private static final String ES_TAG_CONF_THREAD_NAME = "EsTagConf";

  IDBPersistenceHandler<EsTagConfig> esTagConfigIndexer;

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
  public EsTagConfigListener(@Qualifier("esTagConfigIndexer") final IDBPersistenceHandler<EsTagConfig> esTagConfigIndexer,
                             final EsTagConfigConverter configConverter) {
    this.esTagConfigIndexer = esTagConfigIndexer;
    this.configConverter = configConverter;

    log.info("ESTagConfigListener is running");
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
  public void notifyElementUpdated(Collection<Tag> collection) {
    if (collection == null) {
      log.warn("notifyElementUpdated() = Received a null collection of tags");
      return;
    }
    log.info("notifyElementUpdated() - Received a collection of " + collection.size() + " elements");

    try {
      esTagConfigIndexer.storeData(convertTagsToEsTags(collection));
    }
    catch (IDBPersistenceException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void confirmStatus(Collection<Tag> eventCollection) {

  }

  @Override
  public String getThreadName() {
    return ES_TAG_CONF_THREAD_NAME;
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
