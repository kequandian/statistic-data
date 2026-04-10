package com.jfeat.am.module.statistics.services.cache;

import com.jfeat.am.module.statistics.services.converter.StatisticData;

/**
 * Statistics Cache Service
 * Provides caching for statistics data based on pattern
 */
public interface StatisticCacheService {

    /**
     * Generate cache key for statistics data
     * @param field statistics field name
     * @param pattern data pattern (Count, Rate, Tuple, Gauge, etc.)
     * @param identifier data identifier (optional)
     * @return cache key
     */
    String generateCacheKey(String field, String pattern, String identifier);

    /**
     * Get cached statistics data
     * @param field statistics field name
     * @param pattern data pattern
     * @param identifier data identifier
     * @return cached data or null if not cached
     */
    Object getCachedData(String field, String pattern, String identifier);

    /**
     * Cache statistics data
     * @param field statistics field name
     * @param pattern data pattern
     * @param identifier data identifier
     * @param data data to cache
     */
    void cacheData(String field, String pattern, String identifier, Object data);

    /**
     * Evict cached data for specific field
     * @param field statistics field name
     */
    void evictField(String field);

    /**
     * Evict cached data for specific field and pattern
     * @param field statistics field name
     * @param pattern data pattern
     */
    void evictFieldPattern(String field, String pattern);

    /**
     * Evict all cached statistics data
     */
    void evictAll();
}
