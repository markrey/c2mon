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
package cern.c2mon.pmanager;

import java.util.List;

import cern.c2mon.pmanager.persistence.exception.IDBPersistenceException;

/**
 * Interface exposing the functionality that any class willing to store incoming data to
 * the server to TIMDB should implement
 * @author mruizgar
 *
 */
public interface IDBPersistenceHandler<T extends IFallback> {


    /**
     * Gets a string identifying the user account and database with which this class operates
     * @return String identifying the database account used by this class
     */
    String getDBInfo();

    /** Stores an IFallback object into a DB table
     *  @param object IFallback object containing the data to be committed to the DB
     *  @throws IDBPersistenceException An exception is thrown in case the object cannot be committed to the DB
     */
     void storeData(T object) throws IDBPersistenceException;

     /** Stores a list of incoming objects into a db table
     *  @param data List of IFallback objects containing the data to be committed to the DB
     *  @throws IDBPersistenceException An exception is thrown in case the objects cannot be committed to the DB
     */
    void storeData(List<T> data) throws IDBPersistenceException;
}


