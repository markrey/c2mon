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

package cern.c2mon.server.eslog.structure.types.tag;

import com.google.gson.Gson;
import lombok.Data;
import lombok.NonNull;

import cern.c2mon.pmanager.IFallback;
import cern.c2mon.pmanager.fallback.exception.DataFallbackException;
import cern.c2mon.server.eslog.structure.types.GsonSupplier;

/**
 * @author Szymon Halastra
 */
@Data
public class EsTagConfig implements IFallback {

  @NonNull
  protected static final transient Gson gson = GsonSupplier.INSTANCE.get();

  private final EsTagC2monInfo c2mon;

  public EsTagConfig() {
    this.c2mon = new EsTagC2monInfo("String");
  }

  @Override
  public IFallback getObject(String line) throws DataFallbackException {
    return null;
  }

  @Override
  public String getId() {
    return null;
  }
}
