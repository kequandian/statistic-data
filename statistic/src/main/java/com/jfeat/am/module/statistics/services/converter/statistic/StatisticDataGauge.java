package com.jfeat.am.module.statistics.services.converter.statistic;

import com.jfeat.am.module.statistics.services.converter.StatisticData;

import java.util.HashMap;
import java.util.Map;

/**
 * Gauge pattern data structure
 * Returns simple key-value pairs: {"<recordName>":"<recordValue>"}
 */
public class StatisticDataGauge extends StatisticData {
    private Map<String, String> gauge;

    public StatisticDataGauge() {
        this.gauge = new HashMap<>();
    }

    public Map<String, String> getGauge() {
        return gauge;
    }

    public void setGauge(Map<String, String> gauge) {
        this.gauge = gauge;
    }

    public StatisticDataGauge addMetric(String name, String value) {
        if (this.gauge == null) {
            this.gauge = new HashMap<>();
        }
        this.gauge.put(name, value);
        return this;
    }
}
