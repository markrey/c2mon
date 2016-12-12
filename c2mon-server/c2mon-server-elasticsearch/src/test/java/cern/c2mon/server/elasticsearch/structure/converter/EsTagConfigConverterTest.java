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

package cern.c2mon.server.elasticsearch.structure.converter;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.elasticsearch.structure.types.tag.EsTagConfig;
import cern.c2mon.shared.common.metadata.Metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;


/**
 * @author Szymon Halastra
 */
@RunWith(MockitoJUnitRunner.class)
public class EsTagConfigConverterTest {

  @InjectMocks
  EsTagConfigConverter esTagConfigConverter;

  @Mock
  Tag tag;


  final long ID = 1L;
  final String NAME = "TAG";

  final Metadata METADATA = new Metadata();

  @Before
  public void setUp() throws Exception {
    reset(tag);

    METADATA.getMetadata().put("responsible", "John");
    METADATA.getMetadata().put("building", "444");
  }

  @Test
  public void convertTag() {
    when(tag.getId()).thenReturn(ID);
    when(tag.getName()).thenReturn(NAME);
    when(tag.getMetadata()).thenReturn(METADATA);

    EsTagConfig esTagConfig = esTagConfigConverter.convert(tag);

    assertEquals(esTagConfig.getId(), ID);
    assertEquals(esTagConfig.getName(), NAME);

    assertNotNull(esTagConfig.getMetadata());

    assertEquals("John", esTagConfig.getMetadata().get("responsible"));
    assertEquals("444", esTagConfig.getMetadata().get("building"));
  }
}
