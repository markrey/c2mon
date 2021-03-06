/******************************************************************************
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
 *****************************************************************************/
package cern.c2mon.server.cache;

import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.process.Process;

/**
 * The module public interface that should be used to access the Equipment
 * in the server cache.
 *
 * <p>It provides methods for retrieving references to the objects in the
 * cache, which may be accessed by other threads concurrently. To guarantee
 * exclusive access the thread must synchronize on the Equipment object in
 * the cache.
 *
 * @author Mark Brightwell
 *
 */
public interface ProcessCache extends C2monCacheWithListeners<Long, Process> {

  String cacheInitializedKey = "c2mon.cache.process.initialized";

  /**
   * Retrieves a copy of the process in the cache given its name.
   *
   * <p>Throws an {@link IllegalArgumentException} if called with a null key and
   * a {@link CacheElementNotFoundException} if the object was not found in the
   * cache (both unchecked).
   *
   *
   * @param name the process name
   * @return a reference to the object in the cache
   */
  Process getCopy(String name);

  /**
   * Returns the process id given the name.
   * @param name the process name
   * @return the process id
   */

  Long getProcessId(String name);

  /**
   * Retrieve the number of tags currently configured for a given process.
   *
   * @param processId the ID of the process
   * @return the number of tags configured for the process
   */
  Integer getNumTags(Long processId);

  /**
   * Retrieve the number of currently configured tags that are invalid for a
   * process.
   *
   * @param processId the ID of the process
   * @return the number of invalid tags configured for the process
   */
  Integer getNumInvalidTags(Long processId);
}
