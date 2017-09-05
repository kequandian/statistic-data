package com.jfeat.am.modular.statistic.service;

import com.jfeat.am.common.persistence.model.StatisticRecord;
import com.jfeat.am.modular.statistic.mq.StatisticNotifyData;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Silent-Y on 2017/8/31.
 */
public interface StatisticRecordService {

    public boolean insertStatisticRecord(StatisticNotifyData memberAnalysisNotifyData);

    public List<Map<String,String>> getStatisticRecordByTypeIdAndStartTimeAndEndTime(Long typeId,List<String> fields,String startTime,String endTime);
}
