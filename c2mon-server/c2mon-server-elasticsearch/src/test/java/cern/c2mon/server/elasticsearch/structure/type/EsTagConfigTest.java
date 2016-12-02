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

package cern.c2mon.server.elasticsearch.structure.type;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.elasticsearch.structure.types.tag.EsTagConfig;
import cern.c2mon.server.test.CacheObjectCreation;

import static org.junit.Assert.assertEquals;

/**
 * @author Szymon Halastra
 */
@RunWith(JUnit4.class)
public class EsTagConfigTest {

  private Gson gson = new GsonBuilder().create();

  private EsTagConfig esTagConfig;

  private String expectedJson;

  @Before
  public void setup() {
    DataTagCacheObject tag = CacheObjectCreation.createTestDataTag();

    Map<String, String> metadata = new HashMap<>();
    metadata.put("test", "test");

    esTagConfig = new EsTagConfig(1L, "test", Long.class.getSimpleName(), metadata);

    expectedJson = gson.toJson(esTagConfig);
  }

  @Test
  public void checkJsonConversion() {
    assertEquals(expectedJson, esTagConfig.toString());
  }
}
