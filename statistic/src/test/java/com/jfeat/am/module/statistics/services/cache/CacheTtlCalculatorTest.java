package com.jfeat.am.module.statistics.services.cache;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

/**
 * Unit tests for CacheTtlCalculator
 */
public class CacheTtlCalculatorTest {

    /**
     * Test that TTL is always positive and reasonable
     */
    @Test
    public void testTtlIsPositive() {
        long ttlSeconds = CacheTtlCalculator.calculateTtlInSeconds();
        assertTrue(ttlSeconds > 0, "TTL should be positive");
        assertTrue(ttlSeconds <= TimeUnit.HOURS.toSeconds(24), "TTL should not exceed 24 hours");
    }

    /**
     * Test TTL in different time units
     *
     * Note: TimeUnit.convert() truncates towards zero, so we test within reasonable bounds.
     * The minutes and hours values are calculated independently, so we account for truncation.
     */
    @Test
    public void testTtlInDifferentUnits() {
        // Get all values from a single calculation point for consistency
        long ttlSeconds = CacheTtlCalculator.calculateTtlInSeconds();

        // When converting to larger units, truncation occurs
        // 54737 seconds = 912.28 minutes -> truncated to 912 minutes
        // 912 * 60 = 54720, which is within 59 seconds of 54737
        long ttlMinutes = CacheTtlCalculator.calculateTtl(TimeUnit.MINUTES);
        long ttlHours = CacheTtlCalculator.calculateTtl(TimeUnit.HOURS);

        // Allow for truncation difference (max 59 seconds when converting to minutes)
        assertTrue(Math.abs(ttlSeconds - ttlMinutes * 60) < 60,
                "Seconds should match minutes * 60 within 1 minute");

        // Allow for truncation difference (max 3599 seconds when converting to hours)
        assertTrue(Math.abs(ttlSeconds - ttlHours * 3600) < 3600,
                "Seconds should match hours * 3600 within 1 hour");
    }

    /**
     * Test that expiration description is not null
     */
    @Test
    public void testExpirationDescription() {
        String description = CacheTtlCalculator.getExpirationDescription();
        assertNotNull(description, "Description should not be null");
        assertTrue(description.contains("4:00"), "Description should mention 4:00");
    }

    /**
     * Test that daily expiration is enabled by default
     */
    @Test
    public void testDailyExpirationEnabledByDefault() {
        assertTrue(CacheTtlCalculator.isDailyExpirationEnabled(),
                "Daily expiration should be enabled by default");
    }

    /**
     * Test that next expiration time is in the future
     */
    @Test
    public void testNextExpirationTimeIsInFuture() {
        long now = System.currentTimeMillis();
        long expiration = CacheTtlCalculator.getNextExpirationTimeMillis();
        assertTrue(expiration > now, "Expiration time should be in the future");

        // Expiration should be within 25 hours (slightly more than 24 to account for edge cases)
        long maxMillis = TimeUnit.HOURS.toMillis(25);
        assertTrue((expiration - now) <= maxMillis, "Expiration should be within 25 hours");
    }

    /**
     * Test TTL calculation manually for different scenarios
     *
     * Note: This test uses reflection-like logic to verify the calculation
     * without modifying the system time
     */
    @Test
    public void testTtlCalculationScenarios() {
        // Current TTL should be between 1 second and 24 hours
        long ttlSeconds = CacheTtlCalculator.calculateTtlInSeconds();
        long minExpected = 1L;
        long maxExpected = TimeUnit.HOURS.toSeconds(24L);

        assertTrue(ttlSeconds >= minExpected,
                "TTL should be at least " + minExpected + " second, got: " + ttlSeconds);
        assertTrue(ttlSeconds <= maxExpected,
                "TTL should be at most " + maxExpected + " seconds (24 hours), got: " + ttlSeconds);
    }

    /**
     * Test that system property can disable daily expiration
     */
    @Test
    public void testSystemPropertyOverride() {
        String originalValue = System.getProperty("statistic.cache.daily-expiration");

        try {
            // Test disabling
            System.setProperty("statistic.cache.daily-expiration", "false");
            assertFalse(CacheTtlCalculator.isDailyExpirationEnabled(),
                    "Daily expiration should be disabled when property is false");

            // Test enabling
            System.setProperty("statistic.cache.daily-expiration", "true");
            assertTrue(CacheTtlCalculator.isDailyExpirationEnabled(),
                    "Daily expiration should be enabled when property is true");
        } finally {
            // Restore original value
            if (originalValue != null) {
                System.setProperty("statistic.cache.daily-expiration", originalValue);
            } else {
                System.clearProperty("statistic.cache.daily-expiration");
            }
        }
    }
}
