package com.shishir.ticketmetrics.testsupport.utl;

import org.springframework.cache.CacheManager;

import java.util.Optional;

public class CacheTestUtil {
  public static void clearCache(CacheManager cacheManager) {
    cacheManager.getCacheNames()
        .forEach(cacheName ->
            Optional.ofNullable(cacheManager.getCache(cacheName))
                .ifPresent(cache -> cache.clear())
        );
  }
}
