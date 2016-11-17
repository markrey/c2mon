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

package cern.c2mon.server.eslog.structure.mappings;

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

  protected Routing _routing;
  protected final Properties properties;
  private static transient Gson gson = new GsonBuilder().setPrettyPrinting().create();

  public EsTagConfigMapping(String esTagConfigType, String dataType) {
    this._routing = new Routing();
    this.properties = new Properties(esTagConfigType, dataType);
  }

  @Override
  public String getMapping() {
    String json = gson.toJson(this);
    log.trace("getMapping() - Created the mapping : " + json);
    return json;
  }

  @Getter
  private class Routing {
    final String required = "true";
  }

  @Getter
  class Properties {
    Id id;
    Name name;

    /** Valid types are: "number", "boolean", "string", "object" */
    Type type;
    Unit unit;


    C2monMetadata c2mon;
    Metadata metadata;

    /**
     * @param esTagType The ES tag type set in {@link EsTag}
     * @param dataType The data type to the corresponding tag
     */
    Properties(String esTagType, String dataType) {
      this.id = new Id();
      this.name = new Name();

      this.type = new Type();
      this.unit = new Unit();

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

    class SourceTimestamp extends Timestamp {
    }

    class ServerTimestamp extends Timestamp {
    }

    class DaqTimestamp extends Timestamp {
    }

    class Type {
      private String type = ValueType.STRING.toString();
      private final String index = indexNotAnalyzed;
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

    class StatusInfo {
      private final String type = ValueType.STRING.toString();
      private final String index = indexNotAnalyzed;
    }

    //c2mon goes here
    class C2monMetadata {
      private final String dynamic = "false";
      private final String type = ValueType.OBJECT.toString();

      private final Map<String, Object> properties = new HashMap<String, Object>(){{
        put("process", new Process());
        put("equipment", new Equipment());
        put("subEquipment", new SubEquipment());

        put("dataType", new DataType());

        put("serverTimestamp", new ServerTimestamp());
        put("sourceTimestamp", new SourceTimestamp());
        put("daqTimestamp", new DaqTimestamp());
      }};
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
