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
package cern.c2mon.shared.client.tag;

import java.sql.Timestamp;
import java.util.Collection;

import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.shared.client.request.ClientRequestResult;
import cern.c2mon.shared.common.datatag.DataTagQuality;

/**
 * This interface defines the tag value updates that are sent to the client layer.
 * Please note that this interface only defines methods for values that can be changed
 * by the source. All static tag specification methods are defined by <code>TransferTag</code>
 * interface and shall be sent in a separate message.
 *
 * @author Matthias Braeger
 * @see TagUpdate
 */
public interface TagValueUpdate extends ClientRequestResult {
  /**
   * Returns the tag identifier
   * @return the tag identifier
   */
  Long getId();

  /**
   * Returns DataTagQuality object
   * @return the DataTagQuality object for this data tag.
   */
  DataTagQuality getDataTagQuality();

  /**
   * Returns the tag value
   * @return the tag value
   */
  Object getValue();

  /**
   * This timestamp indicates when the tag value event was generated by
   * the source.
   * @return the source timestamp of the tag
   */
  Timestamp getSourceTimestamp();

  /**
   * This timestamp indicates when the tag value passed the DAQ module.
   * Please notice that the source timestamp and DAQ timestamp might
   * be the same in case that the value change event was generated by the
   * DAQ itself. It can also be <code>null</code>, if the value update
   * was generated by the server, which can be the case when the communication
   * to the DAQ layer is lost.
   * @return the DAQ tag time stamp, or <code>null</code>
   */
  Timestamp getDaqTimestamp();

  /**
   * This timestamp indicates when the tag value passed the server.
   * Please notice that the source timestamp and server timestamp might
   * be the same in case that the value change event was generated by the
   * server itself.
   * @return the server tag time stamp
   */
  Timestamp getServerTimestamp();

  /**
   * Returns the tag description
   * @return the tag description
   */
  String getDescription();

  /**
   * Returns the collection of registered alarms for that tag
   * @return The collection of registered alarms or an empty list, if no alarm is defined
   */
  Collection<AlarmValue> getAlarms();

  /**
   * Returns the current mode of the tag.
   * @return Returns either OPERATIONAL, TEST or MAINTENANCE
   */
  TagMode getMode();

  /**
   * @return <code>true</code>, if the tag value is currently simulated and not
   *         corresponding to a live event.
   */
  boolean isSimulated();

  /**
   * Returns the tag value description
   * @return the tag value description
   */
  String getValueDescription();


  /**
   * Returns the full class name of the tag value.
   * @return
   */
  String getValueClassName();

  /**
   * Set the tag value;
   */
  void setValue(Object arg);
}