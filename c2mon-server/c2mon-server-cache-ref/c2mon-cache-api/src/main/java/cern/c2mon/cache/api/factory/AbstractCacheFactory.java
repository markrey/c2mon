package cern.c2mon.cache.api.factory;

import cern.c2mon.cache.api.Cache;

/**
 * @author Szymon Halastra
 */
public abstract class AbstractCacheFactory {

  public AbstractCacheFactory getFactory() {
    AbstractCacheFactory abstractFactory = getCachingFactory();

    return abstractFactory;
  }

  public abstract Cache createCache(String name, Class<?> keyType, Class<?> valueType);

  public abstract AbstractCacheFactory getCachingFactory();
}