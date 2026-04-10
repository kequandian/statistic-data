package com.jfeat.am.module.statistics.services.cache.impl;

import com.jfeat.am.module.statistics.services.cache.StatisticCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * In-memory implementation of StatisticCacheService
 * Uses ConcurrentHashMap for thread-safe caching
 * Fallback when Redis is not available
 */
@Service("memoryStatisticCacheService")
public class MemoryStatisticCacheService implements StatisticCacheService {

    protected static final Logger logger = LoggerFactory.getLogger(MemoryStatisticCacheService.class);

    private static final String CACHE_PREFIX = "stat:";
    private static final long DEFAULT_TTL_MINUTES = 5;

    // Cache entry with expiration time
    private static class CacheEntry {
        final Object data;
        final long expireTime;

        CacheEntry(Object data) {
            this.data = data;
            this.expireTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(DEFAULT_TTL_MINUTES);
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
    }

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    @Override
    public String generateCacheKey(String field, String pattern, String identifier) {
        StringBuilder key = new StringBuilder(CACHE_PREFIX);
        key.append(field);
        key.append(":");
        key.append(pattern);
        if (identifier != null && !identifier.isEmpty()) {
            key.append(":");
            key.append(identifier);
        }
        return key.toString();
    }

    @Override
    public Object getCachedData(String field, String pattern, String identifier) {
        String key = generateCacheKey(field, pattern, identifier);
        try {
            CacheEntry entry = cache.get(key);
            if (entry != null) {
                if (entry.isExpired()) {
                    cache.remove(key);
                    logger.debug("Cache expired for key: {}", key);
                    return null;
                }
                logger.debug("Cache hit for key: {}", key);
                return entry.data;
            }
            logger.debug("Cache miss for key: {}", key);
            return null;
        } catch (Exception e) {
            logger.warn("Failed to get cached data for key: {}", key, e);
            return null;
        }
    }

    @Override
    public void cacheData(String field, String pattern, String identifier, Object data) {
        String key = generateCacheKey(field, pattern, identifier);
        try {
            cache.put(key, new CacheEntry(data));
            logger.debug("Cached data for key: {}, TTL: {} minutes", key, DEFAULT_TTL_MINUTES);
        } catch (Exception e) {
            logger.warn("Failed to cache data for key: {}", key, e);
        }
    }

    @Override
    public void evictField(String field) {
        try {
            String prefix = CACHE_PREFIX + field + ":";
            cache.keySet().removeIf(key -> key.startsWith(prefix));
            logger.debug("Evicted all cache entries for field: {}", field);
        } catch (Exception e) {
            logger.warn("Failed to evict cache for field: {}", field, e);
        }
    }

    @Override
    public void evictFieldPattern(String field, String pattern) {
        try {
            String prefix = CACHE_PREFIX + field + ":" + pattern + ":";
            cache.keySet().removeIf(key -> key.startsWith(prefix));
            logger.debug("Evicted cache entries for field: {}, pattern: {}", field, pattern);
        } catch (Exception e) {
            logger.warn("Failed to evict cache for field: {}, pattern: {}", field, pattern, e);
        }
    }

    @Override
    public void evictAll() {
        try {
            cache.clear();
            logger.debug("Evicted all statistics cache entries");
        } catch (Exception e) {
            logger.warn("Failed to evict all cache entries", e);
        }
    }
}
