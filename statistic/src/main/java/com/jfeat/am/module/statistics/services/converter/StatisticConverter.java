package com.jfeat.am.module.statistics.services.converter;

import com.jfeat.am.module.statistics.services.converter.statistic.*;
import com.jfeat.am.module.statistics.services.crud.model.StatisticsFieldModel;
import com.jfeat.am.module.statistics.services.persistence.model.StatisticsRecord;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Code Generator on 2017-11-25
 */
public class StatisticConverter {
    protected static final Logger logger = LoggerFactory.getLogger(StatisticConverter.class);
    /**
     * Pattern Count
     * @param model
     * @return
     */
    public static StatisticDataCount convertStatisticCount(StatisticsFieldModel model){
        StatisticDataCount counter = new StatisticDataCount();
        counter.setField(model.getField());
        counter.setTitle(model.getName());
        counter.setPattern(model.getPattern());
        counter.setChart(model.getChart());
        counter.setSpan(model.getAttrSpan());

        counter.setName(model.getName());

        if(model.getItems() != null && !model.getItems().isEmpty()) {
            StatisticsRecord record = model.getItems().get(0);
            counter.setValue(record.getRecordValue());
            counter.setIdentifier(record.getIdentifier());
        }

        return counter;
    }
    public static StatisticDataCountTimeline convertStatisticCountTimeline(StatisticsFieldModel model){
        StatisticDataCountTimeline countTimeline = new StatisticDataCountTimeline();
        countTimeline.setField(model.getField());
        countTimeline.setTitle(model.getName());
        countTimeline.setPattern(model.getPattern());
        countTimeline.setChart(model.getChart());
        countTimeline.setSpan(model.getAttrSpan());

        countTimeline.setName(model.getName());
        countTimeline.setTimeline(new ArrayList<>());

        /// add rate
        List<Map<String,StatisticDataNameValue>> timeline = countTimeline.getTimeline();
        for (StatisticsRecord record : model.getItems()){
            if(countTimeline.getIdentifier()==null){
                countTimeline.setIdentifier(record.getIdentifier());
            }

            Map<String,StatisticDataNameValue> stat = new HashMap<>();
            stat.put(record.getRecordTimeline(),
                    new StatisticDataNameValue(record.getSeq(), record.getIdentifier(),
                            record.getRecordName(), record.getRecordValue()));

            timeline.add(stat);
        }
        return countTimeline;
    }
    public static StatisticDataCountCluster convertStatisticCountCluster(StatisticsFieldModel model){
        //TODO,
        throw new NotImplementedException("Not implement StatisticDataCountCluster");
    }
    public static StatisticDataCountTimelineCluster convertStatisticCountTimelineCluster(StatisticsFieldModel model){
        //TODO,
        throw new NotImplementedException("Not Implement StatisticDataCountTimelineCluster");
    }

    /**
     * Pattern Rate
     * @param model
     * @return
     */
    public static StatisticDataRate convertStatisticRate(StatisticsFieldModel model){
        StatisticDataRate rate = new StatisticDataRate();
        rate.setField(model.getField());
        rate.setTitle(model.getName());
        rate.setPattern(model.getPattern());
        rate.setChart(model.getChart());
        rate.setSpan(model.getAttrSpan());

        rate.setName(model.getName());
        rate.setRates(new ArrayList<>());

        /// add rate
        List<StatisticDataNameValue> rates = rate.getRates();
        if(model.getItems()!=null && model.getItems().size()>0) {
            for (StatisticsRecord record : model.getItems()) {
                if(rate.getIdentifier()==null){
                    rate.setIdentifier(record.getIdentifier());
                }
                rates.add(new StatisticDataNameValue(
                        record.getSeq(), record.getIdentifier(),
                        record.getRecordName(), record.getRecordValue()));
            }
        }
        rates = rates.stream().sorted(Comparator.comparing(StatisticDataNameValue::getSeq)).collect(Collectors.toList());
        rate.setRates(rates);
        return rate;
    }
    public static StatisticDataRateTimeline convertStatisticRateTimeline(StatisticsFieldModel model){
        StatisticDataRateTimeline rateTimeline = new StatisticDataRateTimeline();
        rateTimeline.setField(model.getField());
        rateTimeline.setName(model.getName());
        rateTimeline.setPattern(model.getPattern());
        rateTimeline.setChart(model.getChart());
        rateTimeline.setSpan(model.getAttrSpan());

        rateTimeline.setName(model.getName());  ///报表数据域
        rateTimeline.setTimeline(new ArrayList<>());

        /// add rate
        Map<String,StatisticDataRate> hashTemp = new HashMap<>();

        List<StatisticDataRate> timeline = rateTimeline.getTimeline();
        logger.debug("model.getItems() = {}", model.getItems());
        for (StatisticsRecord record : model.getItems()) {
            //从record 获取identifier名称
            if (rateTimeline.getIdentifier() == null) {
                rateTimeline.setIdentifier(record.getIdentifier());
            }

            // add new timeline
            String timelineName = record.getRecordTimeline();
            {
                if (!hashTemp.containsKey(timelineName)) {
                    hashTemp.put(timelineName, new StatisticDataRate(timelineName, new ArrayList<>()));

                    //add new tuple
                    timeline.add(hashTemp.get(timelineName));
                }
            }

            /// append timeline stat
            StatisticDataRate currentTimelineRate = hashTemp.get(timelineName);
            currentTimelineRate.setTl(record.getTimeline());

            currentTimelineRate.getRates().add(new StatisticDataNameValue(record.getSeq(),
                    record.getIdentifier(), record.getRecordName(), record.getRecordValue()));
        }
        timeline.sort((item1, item2) -> item1.getTl().compareTo(item2.getTl()));
        return rateTimeline;
    }

    public static StatisticDataRateCluster convertStatisticRateCluster(StatisticsFieldModel model){
        StatisticDataRateCluster rateCluster = new StatisticDataRateCluster();
        rateCluster.setField(model.getField());
        rateCluster.setName(model.getName());
        rateCluster.setPattern(model.getPattern());
        rateCluster.setChart(model.getChart());
        rateCluster.setSpan(model.getAttrSpan());

        rateCluster.setName(model.getName());  ///报表数据域
        rateCluster.setCluster(new HashedMap());
        for (StatisticsRecord record : model.getItems()) {
            if(rateCluster.getIdentifier() == null) {
                rateCluster.setIdentifier(record.getIdentifier());
            }

            if(!rateCluster.getCluster().containsKey(record.getRecordCluster())) {
                rateCluster.getCluster().put(record.getRecordCluster(), new StatisticDataRate());
                rateCluster.getCluster().get(record.getRecordCluster()).setRates(new ArrayList<>());
            }

            StatisticDataRate statisticDataRate = rateCluster.getCluster().get(record.getRecordCluster());

            statisticDataRate.getRates().add(new StatisticDataNameValue(record.getSeq(),
                    record.getIdentifier(), record.getRecordName(), record.getRecordValue()));
        }
        return rateCluster;
    }
    public static StatisticDataRateTimelineCluster convertStatisticRateTimelineCluster(StatisticsFieldModel model){
        //TODO,
        throw new NotImplementedException("Not Implemented StatisticDataRateTimelineCluster");
    }

    /**
     * Pattern Tuple
     * @param model
     * @return
     */
    public static StatisticDataTuple convertStatisticTuple(StatisticsFieldModel model){
        StatisticDataTuple statisticDataTuple = new StatisticDataTuple();
        statisticDataTuple.setField(model.getField());
        statisticDataTuple.setTitle(model.getName());
        statisticDataTuple.setPattern(model.getPattern());
        statisticDataTuple.setChart(model.getChart());
        statisticDataTuple.setSpan(model.getAttrSpan());

        statisticDataTuple.setName(model.getName());
        statisticDataTuple.setTuple(new ArrayList<>());

        List<StatisticDataNameValue> tuple = statisticDataTuple.getTuple();

        for (StatisticsRecord record : model.getItems()){
            // 从record中获取identifier
            if(statisticDataTuple.getIdentifier()==null){
                statisticDataTuple.setIdentifier(statisticDataTuple.getIdentifier());
            }

            tuple.add(new StatisticDataNameValue(
                    record.getSeq(), record.getIdentifier(),
                    record.getRecordName(), record.getRecordValue()));
        }

        return statisticDataTuple;
    }

    public static StatisticDataTupleTimeline convertStatisticTupleTimeline(StatisticsFieldModel model){
        StatisticDataTupleTimeline tupleTimeline = new StatisticDataTupleTimeline();
        tupleTimeline.setField(model.getField());
        tupleTimeline.setTitle(model.getName());
        tupleTimeline.setPattern(model.getPattern());
        tupleTimeline.setChart(model.getChart());
        tupleTimeline.setSpan(model.getAttrSpan());

        tupleTimeline.setName(model.getName());
        tupleTimeline.setTimeline(new ArrayList<>());
        List<StatisticDataTuple> timeline = tupleTimeline.getTimeline();

        Map<String,StatisticDataTuple> hashTemp = new HashMap<>();

        for(StatisticsRecord record : model.getItems()) {
            //从record 获取identifier名称
            if (tupleTimeline.getIdentifier() == null) {
                tupleTimeline.setIdentifier(record.getIdentifier());
            }

            // add new timeline
            String timelineName = record.getRecordTimeline();
            {
                if (!hashTemp.containsKey(timelineName)) {
                    hashTemp.put(timelineName, new StatisticDataTuple(timelineName, new ArrayList<>()));

                    //add new tuple
                    timeline.add(hashTemp.get(timelineName));
                }
            }

            /// append timeline stat
            StatisticDataTuple currentTimelineTupe = hashTemp.get(timelineName);
            currentTimelineTupe.setTl(record.getTimeline());

            currentTimelineTupe.getTuple().add(new StatisticDataNameValue(record.getSeq(),
                    record.getIdentifier(), record.getRecordName(), record.getRecordValue()));
        }
        timeline.sort((item1, item2) -> (item1.getTl().compareTo(item2.getTl())));
        return tupleTimeline;
    }
    public static StatisticDataTupleCluster convertStatisticTupleCluster(StatisticsFieldModel model){
        StatisticDataTupleCluster tupleCluster = new StatisticDataTupleCluster();
        tupleCluster.setField(model.getField());
        tupleCluster.setTitle(model.getName());
        tupleCluster.setPattern(model.getPattern());
        tupleCluster.setChart(model.getChart());
        tupleCluster.setSpan(model.getAttrSpan());
        tupleCluster.setCluster(new HashedMap());
        for(StatisticsRecord record : model.getItems()) {
            //从record 获取identifier名称
            if (tupleCluster.getIdentifier() == null) {
                tupleCluster.setIdentifier(record.getIdentifier());
            }
            // add new cluster
            String clusterName = record.getRecordCluster();
            if(!tupleCluster.getCluster().containsKey(clusterName)) {
                tupleCluster.getCluster().put(clusterName, new StatisticDataTuple());
                tupleCluster.getCluster().get(clusterName).setTuple(new ArrayList());
            }
            StatisticDataTuple statisticDataTuple = tupleCluster.getCluster().get(clusterName);
            statisticDataTuple.getTuple().add(new StatisticDataNameValue(record.getSeq(),
                    record.getIdentifier(), record.getRecordName(), record.getRecordValue()));
        }
        return tupleCluster;
    }



    public static StatisticDataTupleTimelineCluster convertStatisticTupleTimelineCluster(StatisticsFieldModel model){
        StatisticDataTupleTimelineCluster tupleTimelineCluster = new StatisticDataTupleTimelineCluster();
        tupleTimelineCluster.setField(model.getField());
        tupleTimelineCluster.setTitle(model.getName());
        tupleTimelineCluster.setPattern(model.getPattern());
        tupleTimelineCluster.setChart(model.getChart());
        tupleTimelineCluster.setSpan(model.getAttrSpan());

        tupleTimelineCluster.setName(model.getName());

        Map<String, Map<String,StatisticDataTuple>> hashTemp = new HashMap<>();


        for(StatisticsRecord record : model.getItems()) {
            //从record 获取identifier名称
            if (tupleTimelineCluster.getIdentifier() == null) {
                tupleTimelineCluster.setIdentifier(record.getIdentifier());
            }

            // add new cluster
            String clusterName = record.getRecordCluster();
            if(!hashTemp.containsKey(clusterName)) {

                hashTemp.put(clusterName, new HashMap<>());
            }

            // add new timeline
            Map<String,StatisticDataTuple> clusterTemp = hashTemp.get(clusterName);
            String timelineName = record.getRecordTimeline();
            if(!clusterTemp.containsKey(timelineName)) {
                clusterTemp.put(timelineName, new StatisticDataTuple(timelineName, new ArrayList<>()));
            }

            /// append timeline stat
            StatisticDataTuple currentTimelineTupe = clusterTemp.get(timelineName);

            currentTimelineTupe.setTl(record.getTimeline());

            currentTimelineTupe.getTuple().add(new StatisticDataNameValue(record.getSeq(),
                    record.getIdentifier(), record.getRecordName(), record.getRecordValue()));
        }

        // put
        Map<String,StatisticDataTupleTimeline> clusterTemp = new HashedMap();
        tupleTimelineCluster.setCluster(clusterTemp);
        for(Map.Entry<String, Map<String,StatisticDataTuple>> entry : hashTemp.entrySet()) {
            String clusterName = entry.getKey();
            if(!clusterTemp.containsKey(clusterName)) {
                StatisticDataTupleTimeline tupleTimelineTemp = new StatisticDataTupleTimeline();
                tupleTimelineTemp.setTimeline(new ArrayList<>());
                clusterTemp.put(clusterName, tupleTimelineTemp);
            }
            StatisticDataTupleTimeline tupleTimeline = clusterTemp.get(clusterName);
            for(Map.Entry<String,StatisticDataTuple> values : entry.getValue().entrySet()) {
                String timelineName = values.getKey();
                tupleTimeline.setName("");
                tupleTimeline.getTimeline().add(values.getValue());
            }
        }

        // sort

        return tupleTimelineCluster;
    }

    /**
     * Pattern Gauge
     * Returns simple key-value pairs: {"<recordName>":"<recordValue>"}
     * @param model
     * @return
     */
    public static StatisticDataGauge convertStatisticGauge(StatisticsFieldModel model){
        StatisticDataGauge gaugeData = new StatisticDataGauge();
        gaugeData.setField(model.getField());
        gaugeData.setTitle(model.getName());
        gaugeData.setPattern(model.getPattern());
        gaugeData.setChart(model.getChart());
        gaugeData.setSpan(model.getAttrSpan());

        if(model.getItems() != null && !model.getItems().isEmpty()) {
            for (StatisticsRecord record : model.getItems()) {
                if(gaugeData.getIdentifier() == null){
                    gaugeData.setIdentifier(record.getIdentifier());
                }
                gaugeData.addMetric(record.getRecordName(), record.getRecordValue());
            }
        }

        return gaugeData;
    }
}
