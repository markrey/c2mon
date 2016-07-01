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

package cern.c2mon.server.configuration.parser.configuration;

import cern.c2mon.server.cache.*;
import cern.c2mon.server.cache.loading.SequenceDAO;
import cern.c2mon.server.cache.loading.SubEquipmentDAO;
import cern.c2mon.server.configuration.parser.ConfigurationParser;
import cern.c2mon.server.configuration.parser.exception.ConfigurationParseException;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.api.equipment.SubEquipment;
import cern.c2mon.shared.client.configuration.api.tag.RuleTag;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static cern.c2mon.server.configuration.parser.util.ConfigurationSubEquipmentUtil.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Franz Ritter
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:test-config/server-configuration-parser-test.xml"})
public class ConfigureSubEquipmentTest {

  @Autowired
  ConfigurationParser parser;

  @Autowired
  SequenceDAO sequenceDAO;

  @Autowired
  EquipmentCache equipmentCache;

  @Autowired
  SubEquipmentCache subEquipmentCache;

  @Autowired
  SubEquipmentDAO subEquipmentDAO;

  @Autowired
  ControlTagCache controlTagCache;

  @Autowired
  CommFaultTagCache commFaultTagCache;

  @Autowired
  AliveTimerCache aliveTagCache;

  @Autowired
  TagFacadeGateway tagFacadeGateway;

  @Rule
  public ExpectedException tagException = ExpectedException.none();


  @Before
  public void resetMocks() {
    EasyMock.reset(subEquipmentDAO, sequenceDAO, equipmentCache, subEquipmentCache, equipmentCache, tagFacadeGateway);
  }


  @Test
  public void createSubEquipment() {

    // setup Configuration:
    Properties expectedProps = new Properties();
    SubEquipment subEquipment = buildCreateBasicSubEquipment(expectedProps);

    List<SubEquipment> processList = Arrays.asList(subEquipment);

    Configuration config = new Configuration(1L);
    config.setEntities(processList);

    // setUp Mocks:
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(subEquipmentCache.hasKey(10L)).andReturn(false);
    EasyMock.expect(sequenceDAO.getNextEquipmentId()).andReturn(10L);
    EasyMock.expect(tagFacadeGateway.isInTagCache(102L)).andReturn(false);
    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(102L);
    EasyMock.expect(tagFacadeGateway.isInTagCache(101L)).andReturn(false);
    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(101L);
    EasyMock.expect(tagFacadeGateway.isInTagCache(100L)).andReturn(false);
    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(100L);

    EasyMock.replay(equipmentCache, sequenceDAO, tagFacadeGateway, subEquipmentCache);

    List<ConfigurationElement> parsed = parser.parse(config);

    assertEquals(parsed.size(), 4);

    assertEquals((long) parsed.get(0).getEntityId(), 102L);
    assertTrue(parsed.get(0).getEntity().equals(ConfigConstants.Entity.CONTROLTAG));
    assertTrue(parsed.get(0).getAction().equals(ConfigConstants.Action.CREATE));
    assertEquals(parsed.get(0).getElementProperties().size(), 6);

    assertEquals((long) parsed.get(1).getEntityId(), 101L);
    assertTrue(parsed.get(1).getEntity().equals(ConfigConstants.Entity.CONTROLTAG));
    assertTrue(parsed.get(1).getAction().equals(ConfigConstants.Action.CREATE));
    assertEquals(parsed.get(1).getElementProperties().size(), 6);

    assertEquals((long) parsed.get(2).getEntityId(), 100L);
    assertTrue(parsed.get(2).getEntity().equals(ConfigConstants.Entity.CONTROLTAG));
    assertTrue(parsed.get(2).getAction().equals(ConfigConstants.Action.CREATE));
    assertEquals(parsed.get(2).getElementProperties().size(), 7);

    assertEquals((long) parsed.get(3).getEntityId(), 10L);
    assertTrue(parsed.get(3).getEntity().equals(ConfigConstants.Entity.SUBEQUIPMENT));
    assertTrue(parsed.get(3).getAction().equals(ConfigConstants.Action.CREATE));
    assertEquals(parsed.get(3).getElementProperties(), expectedProps);

    EasyMock.verify(equipmentCache, sequenceDAO, tagFacadeGateway, subEquipmentCache);
  }

  @Test
  public void createSubEquipmentWithAllFields() {
    // setup Configuration:
    Properties expectedProps = new Properties();
    SubEquipment subEquipment = buildCreateAllFieldsSubEquipment(10L, expectedProps);

    List<SubEquipment> subEquipmentList = Arrays.asList(subEquipment);

    Configuration config = new Configuration(1L);
    config.setEntities(subEquipmentList);

    // setUp Mocks:
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(subEquipmentCache.hasKey(10L)).andReturn(false);
    EasyMock.expect(tagFacadeGateway.isInTagCache(102L)).andReturn(false);
    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(102L);
    EasyMock.expect(tagFacadeGateway.isInTagCache(101L)).andReturn(false);
    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(101L);
    EasyMock.expect(tagFacadeGateway.isInTagCache(100L)).andReturn(false);
    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(100L);

    EasyMock.replay(equipmentCache, sequenceDAO, tagFacadeGateway, subEquipmentCache);

    List<ConfigurationElement> parsed = parser.parse(config);

    assertEquals(parsed.size(), 4);

    assertEquals((long) parsed.get(0).getEntityId(), 102L);
    assertTrue(parsed.get(0).getEntity().equals(ConfigConstants.Entity.CONTROLTAG));
    assertTrue(parsed.get(0).getAction().equals(ConfigConstants.Action.CREATE));
    assertEquals(parsed.get(0).getElementProperties().size(), 6);

    assertEquals((long) parsed.get(1).getEntityId(), 101L);
    assertTrue(parsed.get(1).getEntity().equals(ConfigConstants.Entity.CONTROLTAG));
    assertTrue(parsed.get(1).getAction().equals(ConfigConstants.Action.CREATE));
    assertEquals(parsed.get(1).getElementProperties().size(), 6);

    assertEquals((long) parsed.get(2).getEntityId(), 100L);
    assertTrue(parsed.get(2).getEntity().equals(ConfigConstants.Entity.CONTROLTAG));
    assertTrue(parsed.get(2).getAction().equals(ConfigConstants.Action.CREATE));
    assertEquals(parsed.get(2).getElementProperties().size(), 7);

    assertEquals((long) parsed.get(3).getEntityId(), 10L);
    assertTrue(parsed.get(3).getEntity().equals(ConfigConstants.Entity.SUBEQUIPMENT));
    assertTrue(parsed.get(3).getAction().equals(ConfigConstants.Action.CREATE));
    assertEquals(parsed.get(3).getElementProperties(), expectedProps);

    EasyMock.verify(equipmentCache, sequenceDAO, tagFacadeGateway, subEquipmentCache);
  }

  @Test
  public void createMultipleSubEquipmentWithAllFields() {
    Properties expectedProps1 = new Properties();
    Properties expectedProps2 = new Properties();
    Properties expectedProps3 = new Properties();

    SubEquipment equipment1 = buildCreateAllFieldsSubEquipment(10L, expectedProps1);
    SubEquipment equipment2 = buildCreateAllFieldsSubEquipment(11L, expectedProps2);
    SubEquipment equipment3 = buildCreateAllFieldsSubEquipment(12L, expectedProps3);

    List<SubEquipment> subEquipmentList = Arrays.asList(equipment1, equipment2, equipment3);

    Configuration config = new Configuration(1L);
    config.setEntities(subEquipmentList);

    // setUp Mocks:
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(subEquipmentCache.hasKey(10L)).andReturn(false);
    EasyMock.expect(tagFacadeGateway.isInTagCache(102L)).andReturn(false);
    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(102L);
    EasyMock.expect(tagFacadeGateway.isInTagCache(101L)).andReturn(false);
    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(101L);
    EasyMock.expect(tagFacadeGateway.isInTagCache(100L)).andReturn(false);
    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(100L);

    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(subEquipmentCache.hasKey(11L)).andReturn(false);
    EasyMock.expect(tagFacadeGateway.isInTagCache(102L)).andReturn(false);
    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(102L);
    EasyMock.expect(tagFacadeGateway.isInTagCache(101L)).andReturn(false);
    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(101L);
    EasyMock.expect(tagFacadeGateway.isInTagCache(100L)).andReturn(false);
    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(100L);

    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(subEquipmentCache.hasKey(12L)).andReturn(false);
    EasyMock.expect(tagFacadeGateway.isInTagCache(102L)).andReturn(false);
    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(102L);
    EasyMock.expect(tagFacadeGateway.isInTagCache(101L)).andReturn(false);
    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(101L);
    EasyMock.expect(tagFacadeGateway.isInTagCache(100L)).andReturn(false);
    EasyMock.expect(sequenceDAO.getNextTagId()).andReturn(100L);

    EasyMock.replay(equipmentCache, sequenceDAO, tagFacadeGateway, subEquipmentCache);

    List<ConfigurationElement> parsed = parser.parse(config);

    assertEquals(parsed.size(), 12);

    assertEquals((long) parsed.get(0).getEntityId(), 102L);
    assertTrue(parsed.get(0).getEntity().equals(ConfigConstants.Entity.CONTROLTAG));
    assertTrue(parsed.get(0).getAction().equals(ConfigConstants.Action.CREATE));
    assertEquals(parsed.get(0).getElementProperties().size(), 6);

    assertEquals((long) parsed.get(1).getEntityId(), 101L);
    assertTrue(parsed.get(1).getEntity().equals(ConfigConstants.Entity.CONTROLTAG));
    assertTrue(parsed.get(1).getAction().equals(ConfigConstants.Action.CREATE));
    assertEquals(parsed.get(1).getElementProperties().size(), 6);

    assertEquals((long) parsed.get(2).getEntityId(), 100L);
    assertTrue(parsed.get(2).getEntity().equals(ConfigConstants.Entity.CONTROLTAG));
    assertTrue(parsed.get(2).getAction().equals(ConfigConstants.Action.CREATE));
    assertEquals(parsed.get(2).getElementProperties().size(), 7);

    assertEquals((long) parsed.get(3).getEntityId(), 10L);
    assertTrue(parsed.get(3).getEntity().equals(ConfigConstants.Entity.SUBEQUIPMENT));
    assertTrue(parsed.get(3).getAction().equals(ConfigConstants.Action.CREATE));
    assertEquals(parsed.get(3).getElementProperties(), expectedProps1);

    assertEquals((long) parsed.get(4).getEntityId(), 102L);
    assertTrue(parsed.get(4).getEntity().equals(ConfigConstants.Entity.CONTROLTAG));
    assertTrue(parsed.get(4).getAction().equals(ConfigConstants.Action.CREATE));
    assertEquals(parsed.get(4).getElementProperties().size(), 6);

    assertEquals((long) parsed.get(5).getEntityId(), 101L);
    assertTrue(parsed.get(5).getEntity().equals(ConfigConstants.Entity.CONTROLTAG));
    assertTrue(parsed.get(5).getAction().equals(ConfigConstants.Action.CREATE));
    assertEquals(parsed.get(5).getElementProperties().size(), 6);

    assertEquals((long) parsed.get(6).getEntityId(), 100L);
    assertTrue(parsed.get(6).getEntity().equals(ConfigConstants.Entity.CONTROLTAG));
    assertTrue(parsed.get(6).getAction().equals(ConfigConstants.Action.CREATE));
    assertEquals(parsed.get(6).getElementProperties().size(), 7);

    assertEquals((long) parsed.get(7).getEntityId(), 11L);
    assertTrue(parsed.get(7).getEntity().equals(ConfigConstants.Entity.SUBEQUIPMENT));
    assertTrue(parsed.get(7).getAction().equals(ConfigConstants.Action.CREATE));
    assertEquals(parsed.get(7).getElementProperties(), expectedProps2);

    assertEquals((long) parsed.get(8).getEntityId(), 102L);
    assertTrue(parsed.get(8).getEntity().equals(ConfigConstants.Entity.CONTROLTAG));
    assertTrue(parsed.get(8).getAction().equals(ConfigConstants.Action.CREATE));
    assertEquals(parsed.get(8).getElementProperties().size(), 6);

    assertEquals((long) parsed.get(9).getEntityId(), 101L);
    assertTrue(parsed.get(9).getEntity().equals(ConfigConstants.Entity.CONTROLTAG));
    assertTrue(parsed.get(9).getAction().equals(ConfigConstants.Action.CREATE));
    assertEquals(parsed.get(9).getElementProperties().size(), 6);

    assertEquals((long) parsed.get(10).getEntityId(), 100L);
    assertTrue(parsed.get(10).getEntity().equals(ConfigConstants.Entity.CONTROLTAG));
    assertTrue(parsed.get(10).getAction().equals(ConfigConstants.Action.CREATE));
    assertEquals(parsed.get(10).getElementProperties().size(), 7);

    assertEquals((long) parsed.get(11).getEntityId(), 12L);
    assertTrue(parsed.get(11).getEntity().equals(ConfigConstants.Entity.SUBEQUIPMENT));
    assertTrue(parsed.get(11).getAction().equals(ConfigConstants.Action.CREATE));
    assertEquals(parsed.get(11).getElementProperties(), expectedProps3);

    EasyMock.verify(equipmentCache, sequenceDAO, tagFacadeGateway, subEquipmentCache);
  }

  @Test
  public void createSubEquipmentWithNotExistingProcess() {
    // Setup Exception
    tagException.expect(ConfigurationParseException.class);
    tagException.expectMessage("Creating of a new SubEquipment (id = 10) failed: No Equipment with the id 1 found");

    SubEquipment subEquipment = SubEquipment.create("myEquipment").id(10L).build();
    subEquipment.setEquipmentId(1L);

    List<SubEquipment> subEquipmentList = Arrays.asList(subEquipment);

    Configuration config = new Configuration(1L);
    config.setEntities(subEquipmentList);

    // setUp Mocks:

    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(false);

    // run test
    EasyMock.replay(equipmentCache);
    parser.parse(config);
    EasyMock.verify(equipmentCache);
  }

  @Test
  public void createExistingSubEquipment() {
    // Setup Exception
    tagException.expect(ConfigurationParseException.class);
    tagException.expectMessage("Error while parsing a 'create' Configuration: Id 10 of the class SubEquipment already known to the server");

    SubEquipment equipment = SubEquipment.create("E_TEST").id(10L).build();
    equipment.setEquipmentId(1L);

    List<SubEquipment> subEquipmentList = Arrays.asList(equipment);

    Configuration config = new Configuration(1L);
    config.setEntities(subEquipmentList);

    // setUp Mocks:
    EasyMock.expect(equipmentCache.hasKey(1L)).andReturn(true);
    EasyMock.expect(subEquipmentCache.hasKey(10L)).andReturn(true);

    // run test
    EasyMock.replay(subEquipmentCache, equipmentCache);
    parser.parse(config);
    EasyMock.verify(subEquipmentCache, equipmentCache);
  }

  @Test
  public void updateSubEquipmentWithName() {
    // setup Configuration:
    Properties expectedProps = new Properties();
    SubEquipment equipment = buildUpdateSubEquipmentWithSomeFields("myEquipment", expectedProps);

    List<SubEquipment> equipmentUpdateList = Arrays.asList(equipment);

    Configuration config = new Configuration(1L);
    config.setEntities(equipmentUpdateList);

    // setUp Mocks:
    EasyMock.expect(subEquipmentDAO.getIdByName("myEquipment")).andReturn(10L);
    EasyMock.expect(subEquipmentCache.hasKey(10L)).andReturn(true);

    EasyMock.replay(subEquipmentDAO, subEquipmentCache);

    List<ConfigurationElement> parsed = parser.parse(config);

    assertEquals((long) parsed.get(0).getEntityId(), 10L);
    assertEquals(parsed.get(0).getEntity(), ConfigConstants.Entity.SUBEQUIPMENT);
    assertEquals(parsed.get(0).getAction(), ConfigConstants.Action.UPDATE);
    assertEquals(parsed.get(0).getElementProperties(), expectedProps);

    EasyMock.verify(subEquipmentDAO, subEquipmentCache);
  }

  @Test
  public void updateSubEquipmentWithId() {
    // setup Configuration:
    Properties expectedProps = new Properties();
    SubEquipment equipment = buildUpdateSubEquipmentWithSomeFields(10L, expectedProps);

    List<SubEquipment> equipmentUpdateList = Arrays.asList(equipment);

    Configuration config = new Configuration(1L);
    config.setEntities(equipmentUpdateList);

    // setUp Mocks:
    EasyMock.expect(subEquipmentCache.hasKey(10L)).andReturn(true);

    EasyMock.replay(subEquipmentCache);

    List<ConfigurationElement> parsed = parser.parse(config);

    assertEquals((long) parsed.get(0).getEntityId(), 10L);
    assertEquals(parsed.get(0).getEntity(), ConfigConstants.Entity.SUBEQUIPMENT);
    assertEquals(parsed.get(0).getAction(), ConfigConstants.Action.UPDATE);
    assertEquals(parsed.get(0).getElementProperties(), expectedProps);

    EasyMock.verify(subEquipmentCache);
  }

  @Test
  public void updateSubEquipmentWithAllFields() {
    // setup Configuration:
    Properties expectedProps = new Properties();
    SubEquipment equipment = buildUpdateSubEquipmentWithAllFields(10L, expectedProps);

    List<SubEquipment> equipmentUpdateList = Arrays.asList(equipment);

    Configuration config = new Configuration(1L);
    config.setEntities(equipmentUpdateList);

    // setUp Mocks:
    EasyMock.expect(subEquipmentCache.hasKey(10L)).andReturn(true);

    EasyMock.replay(subEquipmentCache);

    List<ConfigurationElement> parsed = parser.parse(config);

    assertEquals((long) parsed.get(0).getEntityId(), 10L);
    assertEquals(parsed.get(0).getEntity(), ConfigConstants.Entity.SUBEQUIPMENT);
    assertEquals(parsed.get(0).getAction(), ConfigConstants.Action.UPDATE);
    assertEquals(parsed.get(0).getElementProperties(), expectedProps);

    EasyMock.verify(subEquipmentCache);
  }

  @Test
  public void updateNonExistingSubEquipment() {
    // Setup Exception
    tagException.expect(ConfigurationParseException.class);
    tagException.expectMessage("Updating of SubEquipment (id = 10) failed: The object is unknown to the sever.");

    // setup Configuration:
    SubEquipment subEquipment = SubEquipment.update(10L).description("The description").build();

    List<SubEquipment> equipmentUpdateList = Arrays.asList(subEquipment);

    Configuration config = new Configuration(1L);
    config.setEntities(equipmentUpdateList);

    // setUp Mocks:
    EasyMock.expect(subEquipmentCache.hasKey(10L)).andReturn(false);

    // run test
    EasyMock.replay(subEquipmentCache);
    parser.parse(config);
    EasyMock.verify(subEquipmentCache);
  }

  @Test
  public void deleteSubEquipment() {
    // setup Configuration:
    SubEquipment subEquipment = buildDeleteSubEquipment(10L);

    List<SubEquipment> equipmentUpdateList = Arrays.asList(subEquipment);

    Configuration config = new Configuration(1L);
    config.setEntities(equipmentUpdateList);

    // setUp Mocks:
    EasyMock.expect(subEquipmentCache.hasKey(10L)).andReturn(true);

    EasyMock.replay(subEquipmentCache);

    List<ConfigurationElement> parsed = parser.parse(config);

    assertEquals(parsed.size(), 1);
    assertEquals((long) parsed.get(0).getEntityId(), 10L);
    assertEquals(parsed.get(0).getAction(), ConfigConstants.Action.REMOVE);
    assertEquals(parsed.get(0).getEntity(), ConfigConstants.Entity.SUBEQUIPMENT);
    assertTrue(parsed.get(0).getElementProperties().isEmpty());

    EasyMock.verify(subEquipmentCache);
  }

  @Test
  public void deleteNotExistingSubEquipment() {
    // Setup Exception
    tagException.expect(ConfigurationParseException.class);
    tagException.expectMessage("Deleting of SubEquipment (id = 10) failed: The object is unknown to the sever.");

    // setup Configuration:
    SubEquipment subEquipment = buildDeleteSubEquipment(10L);

    List<SubEquipment> equipmentRemoveList = Arrays.asList(subEquipment);

    Configuration config = new Configuration(1L);
    config.setEntities(equipmentRemoveList);

    // setUp Mocks:
    EasyMock.expect(subEquipmentCache.hasKey(10L)).andReturn(false);
    EasyMock.replay(subEquipmentCache);

    parser.parse(config);

    EasyMock.verify(subEquipmentCache);
  }
}