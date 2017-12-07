package com.jfeat.am.modular.statistic.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.toolkit.IdWorker;
import com.jfeat.am.modular.statistic.bean.Statistic;
import com.jfeat.am.modular.statistic.bean.StatisticNotifyData;
import com.jfeat.am.modular.statistic.persistence.dao.StatisticFieldMapper;
import com.jfeat.am.modular.statistic.persistence.dao.StatisticRecordMapper;
import com.jfeat.am.modular.statistic.persistence.dao.TypeDefinitionMapper;
import com.jfeat.am.modular.statistic.persistence.model.StatisticField;
import com.jfeat.am.modular.statistic.persistence.model.StatisticRecord;
import com.jfeat.am.modular.statistic.persistence.model.TypeDefinition;
import com.jfeat.am.core.support.DateTimeKit;
import com.jfeat.am.core.support.StrKit;
import com.jfeat.am.modular.statistic.dao.StatisticRecordDao;
import com.jfeat.am.modular.statistic.service.StatisticRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Silent-Y on 2017/8/31.
 */
@Service
public class StatisticRecordServiceImpl extends ServiceImpl<StatisticRecordMapper, StatisticRecord> implements StatisticRecordService {

    @Resource
    TypeDefinitionMapper typeDefinitionMapper;
    @Resource
    StatisticRecordDao statisticRecordDao;
    @Resource
    StatisticFieldMapper statisticFieldMapper;

    @Transactional
    public boolean insertStatisticRecord(StatisticNotifyData statisticNotifyData) {

        List<Statistic> statisticList = statisticNotifyData.getValues();
        Long group = IdWorker.getId();

        //插入type
        TypeDefinition query = new TypeDefinition();
        query.setIdentifier(statisticNotifyData.getIdentifier());
        TypeDefinition typeDefinition = typeDefinitionMapper.selectOne(query);
        if (typeDefinition == null) {
            typeDefinition = new TypeDefinition();
            typeDefinition.setName(statisticNotifyData.getDefaultName());
            typeDefinition.setIdentifier(statisticNotifyData.getIdentifier());
            typeDefinitionMapper.insert(typeDefinition);
        }

        //插入field
        Integer count = statisticFieldMapper.selectCount(new EntityWrapper<StatisticField>().eq("type_id", typeDefinition.getId()));
        if (count < 1) {
            for (Statistic statistic : statisticList) {
                StatisticField statisticField = new StatisticField();
                statisticField.setTypeId(typeDefinition.getId());
                statisticField.setName(statistic.getKey());
                statisticField.setDisplayName(statistic.getName());
                statisticFieldMapper.insert(statisticField);
            }
        }

        //插入record
        for (Statistic statistic : statisticList) {
            StatisticRecord statisticRecord = new StatisticRecord();
            statisticRecord.setRecordTime(statisticNotifyData.getRecordTime());
            statisticRecord.setTypeId(typeDefinition.getId());
            statisticRecord.setFieldName(statistic.getKey());
            statisticRecord.setValue(statistic.getValue());
            statisticRecord.setGroup(group);
            insert(statisticRecord);
        }
        return true;
    }

    public List<Map<String, Object>> getStatisticRecordByTypeIdAndStartTimeAndEndTime(Long typeId, List<String> fields, String startTime, String endTime) {
        if (StrKit.isBlank(startTime)) {
            startTime = DateTimeKit.lastMouth().toString();
        }
        if (StrKit.isBlank(endTime)) {
            endTime = DateTimeKit.formatDateTime(new Date());
        }
        return statisticRecordDao.getStatisticRecordByTypeIdAndStartTimeAndEndTime(typeId, fields, startTime, endTime);
    }
}