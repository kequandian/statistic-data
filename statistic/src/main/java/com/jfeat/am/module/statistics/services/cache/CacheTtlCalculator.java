package com.jfeat.am.module.statistics.services.cache;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

/**
 * Cache TTL Calculator
 *
 * Implements smart TTL calculation for statistics caching:
 * - Cache expires at 4:00 AM the next day
 * - Maximum TTL is 24 hours
 * - Ensures fresh data for daily operations
 *
 * Examples:
 * - If current time is 14:00, TTL = 14 hours (to 04:00 next day)
 * - If current time is 03:00, TTL = 1 hour (to 04:00 today)
 * - If current time is 05:00, TTL = 23 hours (to 04:00 next day)
 */
public class CacheTtlCalculator {

    /**
     * Default expiration time: 4:00 AM
     */
    private static final int EXPIRATION_HOUR = 4;
    private static final int EXPIRATION_MINUTE = 0;

    /**
     * Maximum TTL in hours (24 hours)
     */
    private static final long MAX_TTL_HOURS = 24;

    /**
     * Calculate cache TTL in seconds
     *
     * The cache will expire at 4:00 AM (next occurrence).
     * If the time until 4:00 AM exceeds 24 hours, TTL is capped at 24 hours.
     *
     * @return TTL in seconds
     */
    public static long calculateTtlInSeconds() {
        return calculateTtl(TimeUnit.SECONDS);
    }

    /**
     * Calculate cache TTL in specified time unit
     *
     * @param timeUnit desired time unit
     * @return TTL value in the specified unit
     */
    public static long calculateTtl(TimeUnit timeUnit) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiration = getNextExpirationTime(now);

        long secondsUntilExpiration = java.time.Duration.between(now, expiration).getSeconds();

        // Cap at maximum TTL (24 hours)
        long maxSeconds = TimeUnit.HOURS.toSeconds(MAX_TTL_HOURS);
        long effectiveSeconds = Math.min(secondsUntilExpiration, maxSeconds);

        // Convert to requested time unit
        return timeUnit.convert(effectiveSeconds, TimeUnit.SECONDS);
    }

    /**
     * Get the next 4:00 AM expiration time
     *
     * @param now current time
     * @return next expiration time (4:00 AM)
     */
    private static LocalDateTime getNextExpirationTime(LocalDateTime now) {
        LocalTime expirationTime = LocalTime.of(EXPIRATION_HOUR, EXPIRATION_MINUTE);
        LocalDate today = now.toLocalDate();
        LocalDateTime todayExpiration = LocalDateTime.of(today, expirationTime);

        // If current time is before 4:00 AM today, expire at 4:00 AM today
        // Otherwise, expire at 4:00 AM tomorrow
        if (now.isBefore(todayExpiration)) {
            return todayExpiration;
        } else {
            return todayExpiration.plusDays(1);
        }
    }

    /**
     * Get the next 4:00 AM expiration time as epoch milliseconds
     *
     * @return epoch milliseconds of next expiration time
     */
    public static long getNextExpirationTimeMillis() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiration = getNextExpirationTime(now);
        return expiration.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * Get human-readable description of when cache will expire
     *
     * @return description string
     */
    public static String getExpirationDescription() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiration = getNextExpirationTime(now);

        long hours = java.time.Duration.between(now, expiration).toHours();

        if (expiration.getDayOfMonth() == now.getDayOfMonth()) {
            return String.format("今日 %02d:00 (还有 %d 小时)", EXPIRATION_HOUR, hours);
        } else {
            return String.format("明日 %02d:00 (还有 %d 小时)", EXPIRATION_HOUR, hours);
        }
    }

    /**
     * Check if cache should use 4:00 AM expiration strategy
     * Can be configured via system property or environment variable
     *
     * @return true if 4:00 AM expiration is enabled
     */
    public static boolean isDailyExpirationEnabled() {
        String property = System.getProperty("statistic.cache.daily-expiration", "true");
        String envVar = System.getenv("STATISTIC_CACHE_DAILY_EXPIRATION");

        if (envVar != null && !envVar.isEmpty()) {
            return Boolean.parseBoolean(envVar);
        }
        return Boolean.parseBoolean(property);
    }
}
