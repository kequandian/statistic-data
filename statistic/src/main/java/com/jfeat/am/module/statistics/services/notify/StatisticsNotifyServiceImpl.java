package com.jfeat.am.module.statistics.services.notify;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jfeat.am.module.statistics.services.persistence.dao.StatisticsRecordMapper;
import com.jfeat.am.module.statistics.services.persistence.model.StatisticsRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * Created by Silent-Y on 2017/12/4.
 */
@Deprecated
@Service
public class StatisticsNotifyServiceImpl implements StatisticsNotifyService {

    @Resource
    private StatisticsRecordMapper statisticsRecordMapper;

    @Transactional
    public boolean insertStatisticRecord(StatisticNotifyData statisticNotifyData) {

        List<StatisticChunk> statisticList = statisticNotifyData.getChunks();

        //插入field
        // 指标，这里指 field, 在运维阶段预先设定好即可
        /*for (Statistic statistic : statisticList) {
            Integer count = statisticFieldMapper.selectCount(new EntityWrapper<StatisticField>()
                    .eq("type_id", typeDefinition.getId()).eq("name", statistic.getKey()));
            if (count < 1){
                StatisticsField statisticsField = new StatisticsField();
                statisticsField.setName(statistic.getKey());
                statisticsField.setDisplayName(statistic.getName());
                statisticFieldMapper.insert(statisticsField);
            }
        }*/

        String fieldName = statisticNotifyData.getName();
        Date recordTime = statisticNotifyData.getRecordTime();

        for (StatisticChunk chunk : statisticList) {

            /// 设置记示决定项
            StatisticsRecord record = new StatisticsRecord();
            record.setField(fieldName);                    /// 域名
            record.setRecordName(chunk.getName());         /// 名称
            record.setRecordTuple(chunk.getTuple());       /// 所属行名称
            record.setRecordCluster(chunk.getCluster());   /// 所属分类名称
            record.setTimeline(chunk.getTimeline());       /// 所属时间段名称

            String recordValue = chunk.getValue();

            /// check exists first
//            StatisticsRecord one = statisticsRecordMapper.selectOne(record);
            LambdaQueryWrapper<StatisticsRecord> queryWrapper = Wrappers.<StatisticsRecord>lambdaQuery()
                    .eq(StatisticsRecord::getField,fieldName)
                    .eq(StatisticsRecord::getRecordName,chunk.getName())
                    .eq(StatisticsRecord::getRecordTuple,chunk.getTuple())
                    .eq(StatisticsRecord::getRecordCluster,chunk.getCluster())
                    .eq(StatisticsRecord::getTimeline,chunk.getTimeline());

            StatisticsRecord one = statisticsRecordMapper.selectOne(queryWrapper);
            if(one==null){

                // add new, update value & record time

                record.setRecordValue(recordValue);
                record.setCreateTime(recordTime);

                statisticsRecordMapper.insert(record);

            }else{
                /// just update

                one.setRecordValue(recordValue);
                one.setCreateTime(recordTime);

                statisticsRecordMapper.updateById(one);
            }
        }

        return true;
    }
}
