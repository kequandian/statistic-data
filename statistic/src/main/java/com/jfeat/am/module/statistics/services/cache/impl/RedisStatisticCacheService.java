package com.jfeat.am.module.statistics.services.cache.impl;

import com.jfeat.am.module.statistics.services.cache.CacheTtlCalculator;
import com.jfeat.am.module.statistics.services.cache.StatisticCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * Redis-based implementation of StatisticCacheService
 *
 * Cache keys format: stat:{field}:{pattern}:{identifier}
 *
 * TTL Strategy:
 * - Daily expiration at 4:00 AM (next occurrence)
 * - Maximum TTL: 24 hours
 * - Configurable via system property: statistic.cache.daily-expiration
 */
@Service("redisStatisticCacheService")
public class RedisStatisticCacheService implements StatisticCacheService {

    protected static final Logger logger = LoggerFactory.getLogger(RedisStatisticCacheService.class);

    private static final String CACHE_PREFIX = "stat:";
    private static final long DEFAULT_FALLBACK_TTL_MINUTES = 5;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

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
            Object data = redisTemplate.opsForValue().get(key);
            if (data != null) {
                logger.debug("Cache hit for key: {}", key);
            } else {
                logger.debug("Cache miss for key: {}", key);
            }
            return data;
        } catch (Exception e) {
            logger.warn("Failed to get cached data for key: {}", key, e);
            return null;
        }
    }

    @Override
    public void cacheData(String field, String pattern, String identifier, Object data) {
        String key = generateCacheKey(field, pattern, identifier);
        try {
            long ttl;
            String ttlDescription;

            // Use smart TTL calculation if daily expiration is enabled
            if (CacheTtlCalculator.isDailyExpirationEnabled()) {
                ttl = CacheTtlCalculator.calculateTtlInSeconds();
                ttlDescription = CacheTtlCalculator.getExpirationDescription();

                logger.debug("Cached data for key: {}, expires at 4:00 AM ({})", key, ttlDescription);
            } else {
                // Fallback to simple TTL
                ttl = TimeUnit.MINUTES.toSeconds(DEFAULT_FALLBACK_TTL_MINUTES);
                ttlDescription = DEFAULT_FALLBACK_TTL_MINUTES + " minutes";

                logger.debug("Cached data for key: {}, TTL: {}", key, ttlDescription);
            }

            redisTemplate.opsForValue().set(key, data, ttl, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.warn("Failed to cache data for key: {}", key, e);
        }
    }

    @Override
    public void evictField(String field) {
        try {
            // Evict all keys matching pattern: stat:{field}:*
            String pattern = CACHE_PREFIX + field + ":*";
            redisTemplate.delete(redisTemplate.keys(pattern));
            logger.debug("Evicted all cache entries for field: {}", field);
        } catch (Exception e) {
            logger.warn("Failed to evict cache for field: {}", field, e);
        }
    }

    @Override
    public void evictFieldPattern(String field, String pattern) {
        try {
            // Evict all keys matching pattern: stat:{field}:{pattern}:*
            String keyPattern = CACHE_PREFIX + field + ":" + pattern + ":*";
            redisTemplate.delete(redisTemplate.keys(keyPattern));
            logger.debug("Evicted cache entries for field: {}, pattern: {}", field, pattern);
        } catch (Exception e) {
            logger.warn("Failed to evict cache for field: {}, pattern: {}", field, pattern, e);
        }
    }

    @Override
    public void evictAll() {
        try {
            // Evict all statistics cache keys
            String pattern = CACHE_PREFIX + "*";
            redisTemplate.delete(redisTemplate.keys(pattern));
            logger.debug("Evicted all statistics cache entries");
        } catch (Exception e) {
            logger.warn("Failed to evict all cache entries", e);
        }
    }
}
