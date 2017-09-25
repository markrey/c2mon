package cern.c2mon.server.cache.tag;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractC2monCacheFactory;
import cern.c2mon.server.cache.C2monCacheName;
import cern.c2mon.server.common.tag.Tag;

/**
 * @author Szymon Halastra
 */
@Configuration
public class TagCacheConfig {

  @Bean(name = C2monCacheName.Names.TAG)
  public C2monCache createCache(AbstractC2monCacheFactory cachingFactory) {
    return cachingFactory.createCache(C2monCacheName.TAG.getLabel(), Long.class, Tag.class);
  }
}