package com.jfeat.am.module.statistics.services.crud.converter.statistic;

/**
 * Created by vincent on 2018/5/28.
 */
public class StatisticData {
    public static final String STAT_PATTERN_COUNT = "Count";
    public static final String STAT_PATTERN_COUNT_TIMELINE = "CountTimeline";
    public static final String STAT_PATTERN_COUNT_CLUSTER = "CountCluster";
    public static final String STAT_PATTERN_COUNT_TIMELINE_CLUSTER = "CountTimelineCluster";
    public static final String STAT_PATTERN_RATE = "Rate";
    public static final String STAT_PATTERN_RATE_TIMELINE = "RateTimeline";
    public static final String STAT_PATTERN_RATE_CLUSTER = "RateCluster";
    public static final String STAT_PATTERN_RATE_TIMELINE_CLUSTER = "RateTimelineCluster";
    public static final String STAT_PATTERN_TUPLE = "Tuple";
    public static final String STAT_PATTERN_TUPLE_TIMELINE = "TupleTimeline";
    public static final String STAT_PATTERN_TUPLE_CLUSTER = "TupleCluster";
    public static final String STAT_PATTERN_TUPLE_TIMELINE_CLUSTER = "TupleTimelineCluster";

    /**
     * 检查 统计域 pattern 不否有效
     * @param pattern
     * @return
     */
    public static boolean checkStatisticPattern(String pattern){
        if(pattern==null || pattern.length()==0){
            return false;
        }

        //COUNT
        if(STAT_PATTERN_COUNT.equals(pattern)){
            return true;
        }
        if(STAT_PATTERN_COUNT_TIMELINE.equals(pattern)){
            return true;
        }
        if(STAT_PATTERN_COUNT_CLUSTER.equals(pattern)){
            return true;
        }
        if(STAT_PATTERN_COUNT_TIMELINE_CLUSTER.equals(pattern)){
            return true;
        }
        // RATE
        if(STAT_PATTERN_RATE.equals(pattern)){
            return true;
        }
        if(STAT_PATTERN_RATE_TIMELINE.equals(pattern)){
            return true;
        }
        if(STAT_PATTERN_RATE_CLUSTER.equals(pattern)){
            return true;
        }
        if(STAT_PATTERN_RATE_TIMELINE_CLUSTER.equals(pattern)){
            return true;
        }
        //TUPLE
        if(STAT_PATTERN_TUPLE.equals(pattern)){
            return true;
        }
        if(STAT_PATTERN_TUPLE_TIMELINE.equals(pattern)){
            return true;
        }
        if(STAT_PATTERN_TUPLE_CLUSTER.equals(pattern)){
            return true;
        }
        if(STAT_PATTERN_TUPLE_TIMELINE_CLUSTER.equals(pattern)){
            return true;
        }
        return false;
    }

    /**
     * 记录数据属性
     */
    private String field;
    private String pattern;
    private String chart;
    private int span;  // 所占跨列
    private String identifier;

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getChart() {
        return chart;
    }

    public void setChart(String chart) {
        this.chart = chart;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public int getSpan() {
        return span;
    }

    public void setSpan(int span) {
        this.span = span;
    }
}
