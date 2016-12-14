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

package cern.c2mon.server.elasticsearch.structure.mappings;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Szymon Halastra
 */
@Slf4j
@Data
public class EsTagConfigMapping implements EsMapping {

  protected final Properties properties;
  private static transient Gson gson = new GsonBuilder().setPrettyPrinting().create();

  public EsTagConfigMapping() {
    this.properties = new Properties();
  }

  @Override
  public String getMapping() {
    return gson.toJson(this);
  }

  @Getter
  class Properties {
    Id id;
    Name name;

    /** Valid types are: "number", "boolean", "string", "object" */
    Unit unit;
    Timestamp timestamp;
    Description description;
    Mode mode;
    MaxValue maxValue;
    MinValue minValue;

    C2monMetadata c2mon;
    Metadata metadata;

    StatusInfo statusInfo;

    Properties() {
      this.id = new Id();
      this.name = new Name();

      this.unit = new Unit();
      this.timestamp = new Timestamp();
      this.description = new Description();
      this.mode = new Mode();
      this.maxValue = new MaxValue();
      this.minValue = new MinValue();

      this.c2mon = new C2monMetadata();
      this.metadata = new Metadata();
    }

    class Id {
      private final String type = ValueType.LONG.toString();
    }

    class Name {
      private final String type = ValueType.STRING.toString();
      private final String index = indexNotAnalyzed;
    }

    class DataType {
      private String type = ValueType.STRING.toString();
      private final String index = indexNotAnalyzed;
    }

    class Timestamp {
      private final String type = ValueType.DATE.toString();
      private final String format = epochMillisFormat;
    }

    class Status {
      private final String type = ValueType.INTEGER.toString();
    }

    class Valid {
      private final String type = ValueType.BOOLEAN.toString();
    }

    class Unit {
      private final String type = ValueType.STRING.toString();
      private final String index = indexNotAnalyzed;
    }

    class Description {
      private final String type = ValueType.STRING.toString();
      private final String index= indexNotAnalyzed;
    }

    class Mode {
      private final String type = ValueType.INTEGER.toString();
      private final String index = indexNotAnalyzed;
    }

    class MaxValue {
      private final String type = ValueType.OBJECT.toString();
      private final String index = indexNotAnalyzed;
    }

    class MinValue {
      private final String type = ValueType.OBJECT.toString();
      private final String index = indexNotAnalyzed;
    }

    class StatusInfo {
      private final String type = ValueType.STRING.toString();
      private final String index = indexNotAnalyzed;
    }

    class C2monMetadata {
      private final String dynamic = "false";
      private final String type = ValueType.OBJECT.toString();

      private final Map<String, Object> properties = new HashMap<String, Object>(){{
        put("process", new Process());
        put("equipment", new Equipment());
        put("subEquipment", new SubEquipment());
        put("logged", new Logged());

        put("dataType", new DataType());
      }};
    }

    class Logged {
      private final String type = ValueType.BOOLEAN.toString();
      private final String index = indexNotAnalyzed;
    }

    class Process {
      private final String type = ValueType.STRING.toString();
      private final String index = indexNotAnalyzed;
    }

    class Equipment {
      private final String type = ValueType.STRING.toString();
      private final String index = indexNotAnalyzed;
    }

    class SubEquipment {
      private final String type = ValueType.STRING.toString();
      private final String index = indexNotAnalyzed;
    }

    class Metadata {
      private final String dynamic = "true";
      private final String type = ValueType.NESTED.toString();
    }
  }
}
