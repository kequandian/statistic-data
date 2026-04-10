package com.jfeat.am.module.statistics.services.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

/**
 * Proxy for StatisticCacheService that delegates to the appropriate implementation
 * Uses Redis cache if available, otherwise falls back to memory cache
 */
@Service
public class StatisticCacheProxy {

    protected static final Logger logger = LoggerFactory.getLogger(StatisticCacheProxy.class);

    @Resource(name = "${statistics.cache.impl:redisStatisticCacheService}")
    private StatisticCacheService cacheService;

    @Value("${statistics.cache.enabled:true}")
    private boolean cacheEnabled;

    /**
     * Check if caching is enabled
     */
    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    /**
     * Generate cache key
     */
    public String generateCacheKey(String field, String pattern, String identifier) {
        return cacheService.generateCacheKey(field, pattern, identifier);
    }

    /**
     * Get cached data
     */
    public Object getCachedData(String field, String pattern, String identifier) {
        if (!cacheEnabled) {
            return null;
        }
        return cacheService.getCachedData(field, pattern, identifier);
    }

    /**
     * Cache data
     */
    public void cacheData(String field, String pattern, String identifier, Object data) {
        if (!cacheEnabled) {
            return;
        }
        cacheService.cacheData(field, pattern, identifier, data);
    }

    /**
     * Evict field cache
     */
    public void evictField(String field) {
        if (!cacheEnabled) {
            return;
        }
        cacheService.evictField(field);
    }

    /**
     * Evict field pattern cache
     */
    public void evictFieldPattern(String field, String pattern) {
        if (!cacheEnabled) {
            return;
        }
        cacheService.evictFieldPattern(field, pattern);
    }

    /**
     * Evict all cache
     */
    public void evictAll() {
        if (!cacheEnabled) {
            return;
        }
        cacheService.evictAll();
    }
}
