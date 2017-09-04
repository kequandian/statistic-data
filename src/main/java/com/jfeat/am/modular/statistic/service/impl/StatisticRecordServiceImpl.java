package com.jfeat.am.modular.statistic.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.jfeat.am.common.persistence.dao.StatisticFieldMapper;
import com.jfeat.am.common.persistence.dao.StatisticRecordMapper;
import com.jfeat.am.common.persistence.dao.TypeDefinitionMapper;
import com.jfeat.am.common.persistence.model.StatisticField;
import com.jfeat.am.common.persistence.model.StatisticRecord;
import com.jfeat.am.common.persistence.model.TypeDefinition;
import com.jfeat.am.core.support.BeanKit;
import com.jfeat.am.modular.statistic.mq.StatisticNotifyData;
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
    StatisticFieldMapper statisticFieldMapper;

    @Transactional
    public boolean insertStatisticRecord(StatisticNotifyData statisticNotifyData) {
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
        StatisticField queryStatisticField = new StatisticField();
        queryStatisticField.setTypeId(typeDefinition.getId());
        queryStatisticField.setName(typeDefinition.getName());
        queryStatisticField.setDisplayName(typeDefinition.getName());
        StatisticField statisticField = statisticFieldMapper.selectOne(queryStatisticField);
        if (statisticField == null){
            statisticField = new StatisticField();
            statisticField.setTypeId(typeDefinition.getId());
            statisticField.setName(typeDefinition.getName());
            statisticField.setDisplayName(typeDefinition.getName());
            statisticFieldMapper.insert(statisticField);
        }

        //从 statisticNotifyData.getValue() 迭代取出填到record
        Map<String, String> map = statisticNotifyData.getValue();
        for (Map.Entry<String,String> entry:map.entrySet()){
            StatisticRecord statisticRecord = new StatisticRecord();
            statisticRecord.setRecordTime(statisticNotifyData.getRecordTime());
            statisticRecord.setTypeId(typeDefinition.getId());
            statisticRecord.setFieldName(entry.getKey());
            statisticRecord.setValue(entry.getValue().toString());
            insert(statisticRecord);
        }
        return true;
    }

    public List<StatisticRecord> getStatisticRecordByTypeIdAndStartTimeAndEndTime(long typeId,String startTime,String endTime){

        if (startTime != null && endTime == null){
            return selectList(new EntityWrapper<StatisticRecord>().eq("type_id",typeId).and("record_time > {0}",startTime));
        }
        if (startTime == null && endTime != null){
            String endTimeStr=endTime+" 23:59:59";
            return selectList(new EntityWrapper<StatisticRecord>().eq("type_id",typeId).and("record_time < {0}",endTimeStr));
        }
        if (startTime == null && endTime == null){
            return selectList(new EntityWrapper<StatisticRecord>().eq("type_id",typeId));
        }
        String endTimeStr=endTime+" 23:59:59";
        return selectList(new EntityWrapper<StatisticRecord>().eq("type_id",typeId).between("record_time",startTime,endTimeStr));
    }

    /*select  type_id,record_time,
    sum(case when field_name=‘field1' then value else 0 end) as 'field1',
            sum(case when field_name=‘field2' then value else 0 end) as 'field2'
            from st_statistic_record group by type_id,record_time;*/


}