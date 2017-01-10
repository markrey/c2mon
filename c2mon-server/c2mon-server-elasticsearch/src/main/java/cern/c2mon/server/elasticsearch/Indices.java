package cern.c2mon.server.elasticsearch;

import cern.c2mon.server.elasticsearch.alarm.AlarmDocument;
import cern.c2mon.server.elasticsearch.config.ElasticsearchProperties;
import cern.c2mon.server.elasticsearch.client.ElasticsearchClient;
import cern.c2mon.server.elasticsearch.supervision.SupervisionEventDocument;
import cern.c2mon.server.elasticsearch.tag.TagDocument;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.indices.IndexAlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Justin Lewis Salmon
 */
@Slf4j
@Component
public class Indices {

  private ElasticsearchClient client;

  private ElasticsearchProperties properties;

  private final List<String> indexCache = new CopyOnWriteArrayList<>();

  private static Indices self;

  @Autowired
  public Indices(ElasticsearchProperties properties, ElasticsearchClient client) {
    this.properties = properties;
    this.client = client;;
  }

  @PostConstruct
  public void init() {
    self = this;
  }

  public static boolean create(String indexName) {
    return create(indexName, null, null);
  }

  public static boolean create(String indexName, String type, String mapping) {
    if (exists(indexName)) {
      return true;
    }

    CreateIndexRequestBuilder builder = self.client.getClient().admin().indices().prepareCreate(indexName);
    builder.setSettings(Settings.settingsBuilder()
        .put("number_of_shards", self.properties.getShardsPerIndex())
        .put("number_of_replicas", self.properties.getReplicasPerShard())
        .build());

    if (mapping != null) {
      builder.addMapping(type, mapping);
    }

    log.debug("Creating new index with name {}", indexName);
    boolean created;

    try {
      CreateIndexResponse response = builder.get();
      created = response.isAcknowledged();
    } catch (IndexAlreadyExistsException ex) {
      created = true;
    }

    if (created) {
      self.indexCache.add(indexName);
    }

    return created;
  }

  public static boolean exists(String indexName) {
    if (self.indexCache.contains(indexName)) {
      return true;
    }

    if (self.client.getClient().admin().indices().prepareExists(indexName).get().isExists()) {
      self.indexCache.add(indexName);
      return true;
    }

    return false;
  }

  public static String indexFor(TagDocument tag) {
    String prefix = self.properties.getIndexPrefix() + "-tag_";
    return getIndexName(prefix, (Long) tag.get("timestamp"));
  }

  public static String indexFor(AlarmDocument alarm) {
    String prefix = self.properties.getIndexPrefix() + "-alarm_";
    return getIndexName(prefix, (Long) alarm.get("timestamp"));
  }

  public static String indexFor(SupervisionEventDocument supervisionEvent) {
    String prefix = self.properties.getIndexPrefix() + "-supervision_";
    return getIndexName(prefix, (Long) supervisionEvent.get("timestamp"));
  }

  private static String getIndexName(String prefix, long timestamp) {
    String indexType = self.properties.getIndexType();
    String dateFormat = "yyyy-MM";

    switch (indexType) {
      case "D":
      case "d":
        dateFormat = "yyyy-MM-dd";
        break;
      case "W":
      case "w":
        dateFormat = "yyyy-'W'ww";
        break;
      case "M":
      case "m":
        dateFormat = "yyyy-MM";
        break;
    }

    return prefix + new SimpleDateFormat(dateFormat).format(new Date(timestamp));
  }

  static ElasticsearchProperties getProperties() {
    return self.properties;
  }
}
