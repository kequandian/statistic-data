package com.jfeat.am.module.statistics.services.service.impl;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.jfeat.am.common.crud.impl.CRUDServiceOnlyImpl;
import com.jfeat.am.core.support.DateTimeKit;
import com.jfeat.am.core.support.StrKit;
import com.jfeat.am.module.statistics.services.persistence.dao.StatisticsRecordAttrMapper;
import com.jfeat.am.module.statistics.services.persistence.dao.StatisticsRecordMapper;
import com.jfeat.am.module.statistics.services.persistence.model.StatisticsRecord;
import com.jfeat.am.module.statistics.services.service.StatisticsRecordService;
import com.jfeat.am.module.statistics.services.service.dao.StatisticsRecordDao;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  implementation
 * </p>
 *
 * @author Code Generator
 * @since 2017-11-25
 */
@Service
public class StatisticsRecordServiceImpl extends CRUDServiceOnlyImpl<StatisticsRecord> implements StatisticsRecordService {

    @Resource
    StatisticsRecordMapper statisticsRecordMapper;
    @Resource
    StatisticsRecordAttrMapper statisticsRecordAttrMapper;

    @Resource
    StatisticsRecordDao statisticsRecordDao;

    @Override
    protected BaseMapper<StatisticsRecord> getMasterMapper() {
        return statisticsRecordMapper;
    }

    @Override
    public List<Map<String, Object>> getStatisticsRecordByFieldIdAndStartTimeAndEndTime(String field, List<String> fields, String startTime, String endTime) {
        if (StrKit.isBlank(startTime)) {
            startTime = DateTimeKit.lastMouth().toString();
        }
        if (StrKit.isBlank(endTime)) {
            endTime = DateTimeKit.formatDateTime(new Date());
        }
        return statisticsRecordDao.getStatisticsRecordByFieldIdAndStartTimeAndEndTime(field, fields, startTime, endTime);
    }
}


