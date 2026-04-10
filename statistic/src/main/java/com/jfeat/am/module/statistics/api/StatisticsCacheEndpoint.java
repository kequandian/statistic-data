package com.jfeat.am.module.statistics.api;

import com.jfeat.am.module.statistics.services.cache.StatisticCacheProxy;
import com.jfeat.crud.base.tips.SuccessTip;
import com.jfeat.crud.base.tips.Tip;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * Statistics Cache Management API
 * Provides endpoints for cache management
 */
@Api("统计缓存管理 [Statistics Cache]")
@RestController
@RequestMapping("/api/adm/stat/cache")
public class StatisticsCacheEndpoint {

    @Resource
    private StatisticCacheProxy statisticCacheProxy;

    @ApiOperation("清除指定域的缓存")
    @DeleteMapping("/field/{field}")
    public Tip evictFieldCache(@PathVariable String field) {
        statisticCacheProxy.evictField(field);
        return SuccessTip.create("Cache evicted for field: " + field);
    }

    @ApiOperation("清除指定域和pattern的缓存")
    @DeleteMapping("/field/{field}/pattern/{pattern}")
    public Tip evictFieldPatternCache(@PathVariable String field, @PathVariable String pattern) {
        statisticCacheProxy.evictFieldPattern(field, pattern);
        return SuccessTip.create("Cache evicted for field: " + field + ", pattern: " + pattern);
    }

    @ApiOperation("清除所有统计缓存")
    @DeleteMapping("/all")
    public Tip evictAllCache() {
        statisticCacheProxy.evictAll();
        return SuccessTip.create("All statistics cache evicted");
    }

    @ApiOperation("获取缓存状态")
    @GetMapping("/status")
    public Tip getCacheStatus() {
        return SuccessTip.create(new CacheStatus(statisticCacheProxy.isCacheEnabled()));
    }

    /**
     * Cache status response
     */
    public static class CacheStatus {
        private final boolean enabled;

        public CacheStatus(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isEnabled() {
            return enabled;
        }
    }
}
