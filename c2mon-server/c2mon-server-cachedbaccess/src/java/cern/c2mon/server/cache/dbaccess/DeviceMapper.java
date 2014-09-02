/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 *
 * Copyright (C) 2004 - 2014 CERN. This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * Author: TIM team, tim.support@cern.ch
 ******************************************************************************/
package cern.c2mon.server.cache.dbaccess;

import cern.c2mon.server.common.device.Device;
import cern.c2mon.server.common.device.DeviceCacheObject;

/**
 * MyBatis mapper for for accessing and updating {@link DeviceCacheObject}s in
 * the cache database.
 *
 * @author Justin Lewis Salmon
 */
public interface DeviceMapper extends LoaderMapper<Device> {

  /**
   * Insert a device object from the cache into the db.
   *
   * @param device the device object to insert
   */
  void insertDevice(Device device);

  /**
   * Delete a device object from the db.
   *
   * @param id the ID of the Device object to be deleted
   */
  void deleteDevice(Long id);

  /**
   * Update a device object in the db.
   *
   * @param device the Device object to be updated
   */
  void updateDeviceConfig(Device device);

  /**
   * Delete all property values belonging to a device.
   *
   * @param id the ID of the device from which to delete property values
   */
  void deletePropertyValues(Long id);

  /**
   * Delete all command values belonging to a device.
   *
   * @param id the ID of the device from which to delete command values
   */
  void deleteCommandValues(Long id);
}
