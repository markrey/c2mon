package cern.c2mon.client.core.elasticsearch;

import cern.c2mon.client.core.config.C2monClientProperties;
import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.cache.SubEquipmentCache;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.elasticsearch.Indices;
import cern.c2mon.server.elasticsearch.MappingFactory;
import cern.c2mon.server.elasticsearch.client.ElasticsearchClient;
import cern.c2mon.server.elasticsearch.config.ElasticsearchProperties;
import cern.c2mon.server.elasticsearch.tag.config.TagConfigDocumentConverter;
import cern.c2mon.server.elasticsearch.tag.config.TagConfigDocumentIndexer;
import cern.c2mon.server.elasticsearch.tag.config.TagConfigDocumentListener;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.flush.FlushRequest;
import org.elasticsearch.node.NodeValidationException;
import org.junit.Before;
import org.junit.Test;
import org.powermock.reflect.Whitebox;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.easymock.EasyMock.createNiceMock;
import static org.junit.Assert.assertEquals;

public class ElasticsearchServiceTest {

  private ElasticsearchClient client;
  private TagConfigDocumentListener tagDocumentListener;
  private C2monClientProperties properties = new C2monClientProperties();
  private ElasticsearchProperties elasticsearchProperties = new ElasticsearchProperties();

  public ElasticsearchServiceTest() throws NodeValidationException {
    this.client = new ElasticsearchClient(this.elasticsearchProperties);
    Whitebox.setInternalState(Indices.getInstance(), "client", this.client);
    Whitebox.setInternalState(Indices.getInstance(), "properties", this.elasticsearchProperties);
    TagConfigDocumentIndexer indexer = new TagConfigDocumentIndexer(client, this.elasticsearchProperties);
    ProcessCache processCache = createNiceMock(ProcessCache.class);
    EquipmentCache equipmentCache = createNiceMock(EquipmentCache.class);
    SubEquipmentCache subequipmentCache = createNiceMock(SubEquipmentCache.class);
    TagConfigDocumentConverter converter = new TagConfigDocumentConverter(processCache, equipmentCache, subequipmentCache);
    tagDocumentListener = new TagConfigDocumentListener(indexer, converter);
  }

  @Before
  public void setupElasticsearch() throws InterruptedException, NodeValidationException {
    try {
      CompletableFuture<Void> nodeReady = CompletableFuture.runAsync(() -> {
        client.waitForYellowStatus();
        client.getClient().admin().indices().delete(new DeleteIndexRequest(this.elasticsearchProperties.getTagConfigIndex()));
        Indices.getInstance().create(this.elasticsearchProperties.getTagConfigIndex(), "tag_config", MappingFactory.createTagConfigMapping());
        try {
          Thread.sleep(1000); //it takes some time for the index to be recreated
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      });
      nodeReady.get(120, TimeUnit.SECONDS);
    } catch (ExecutionException | TimeoutException e) {
      throw new RuntimeException("Timeout when waiting for embedded elasticsearch node to start!");
    }
  }

  @Test
  public void testSearchByMetadata() throws InterruptedException {
    try {
      Long testUserTagId = Double.doubleToLongBits(Math.random()) % 10000;
      String testUser = Long.toHexString(Double.doubleToLongBits(Math.random()));
      String responsible = "responsible";
      DataTagCacheObject tag = new DataTagCacheObject(testUserTagId);
      tag.getMetadata().getMetadata().put(responsible, testUser);
      tagDocumentListener.onConfigurationEvent(tag, ConfigConstants.Action.CREATE);

      Long tag1234Id = Double.doubleToLongBits(Math.random()) % 10000;
      String value1234 = "1234";
      tag = new DataTagCacheObject(tag1234Id);
      String key1234 = "1234";
      tag.getMetadata().getMetadata().put(key1234, value1234);
      tagDocumentListener.onConfigurationEvent(tag, ConfigConstants.Action.CREATE);

      client.getClient().admin().indices().flush(new FlushRequest()).actionGet();
      Thread.sleep(10000);

      ElasticsearchService service = new ElasticsearchService(properties);

      assertEquals("There should be 2 tags, one for responsible and one for 1234", 2, service.getDistinctMetadataKeys().size());

      Collection<Long> tagsForResponsibleUser = service.findByMetadata(responsible, testUser);
      assertEquals("There should be one tag with responsible user set to requested value", 1, tagsForResponsibleUser.size());
      assertEquals(testUserTagId, tagsForResponsibleUser.stream().findFirst().get());

      Collection<Long> tags1234 = service.findByMetadata(key1234, value1234);
      assertEquals("There should be one tag with 1234 parameter set to requested value", 1, tags1234.size());
      assertEquals(tag1234Id, tags1234.stream().findFirst().get());
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }

  @Test
  public void testSearchByNameAndMetadata() throws InterruptedException {
    try {
      Long testUserTagId = Double.doubleToLongBits(Math.random()) % 10000;
      String testUser = Long.toHexString(Double.doubleToLongBits(Math.random()));
      String metadataKey = "metadataKey";
      DataTagCacheObject tag = new DataTagCacheObject(testUserTagId);
      String tagname = "tagname";
      tag.setName(tagname);
      tag.getMetadata().getMetadata().put(metadataKey, testUser);
      tagDocumentListener.onConfigurationEvent(tag, ConfigConstants.Action.CREATE);

      tag = new DataTagCacheObject(Double.doubleToLongBits(Math.random()) % 10000);
      tag.setName(tagname);
      tag.getMetadata().getMetadata().put(metadataKey, "some other metadata value");
      tagDocumentListener.onConfigurationEvent(tag, ConfigConstants.Action.CREATE);

      tag = new DataTagCacheObject(Double.doubleToLongBits(Math.random()) % 10000);
      tag.setName("other_tagname");
      tag.getMetadata().getMetadata().put(metadataKey, testUser);
      tagDocumentListener.onConfigurationEvent(tag, ConfigConstants.Action.CREATE);

      client.getClient().admin().indices().flush(new FlushRequest()).actionGet();
      Thread.sleep(10000);

      ElasticsearchService service = new ElasticsearchService(properties);

      Collection<Long> tagsForResponsibleUser = service.findTagsByNameAndMetadata(tagname, metadataKey, testUser);
      assertEquals("There should be one tag with given name and metadata", 1, tagsForResponsibleUser.size());
      assertEquals(testUserTagId, tagsForResponsibleUser.stream().findFirst().get());
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }
}