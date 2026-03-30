package com.jfeat.am.module.statistics.services.crud.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jfeat.am.module.statistics.services.crud.StatisticsFieldService;
import com.jfeat.am.module.statistics.services.crud.model.StatisticsFieldModel;
import com.jfeat.am.module.statistics.services.domain.dao.QueryStatisticsRecordDao;
import com.jfeat.am.module.statistics.services.gen.persistence.dao.StatisticsMetaMapper;
import com.jfeat.am.module.statistics.services.gen.persistence.model.StatisticsMeta;
import com.jfeat.am.module.statistics.services.persistence.dao.StatisticsFieldMapper;
import com.jfeat.am.module.statistics.services.persistence.dao.StatisticsRecordMapper;
import com.jfeat.am.module.statistics.services.persistence.model.StatisticsField;
import com.jfeat.am.module.statistics.services.persistence.model.StatisticsRecord;
import com.jfeat.crud.base.exception.BusinessCode;
import com.jfeat.crud.base.exception.BusinessException;
import com.jfeat.crud.plus.CRUD;
import com.jfeat.crud.plus.CRUDFilter;
import com.jfeat.crud.plus.impl.CRUDServiceOnlyImpl;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * implementation
 * </p>
 *
 * @author Code Generator
 * @since 2017-11-25
 */
@Service
public class StatisticsFieldServiceImpl extends CRUDServiceOnlyImpl<StatisticsField> implements StatisticsFieldService {

    @Resource
    StatisticsFieldMapper statisticsFieldMapper;

    @Resource
    DataSource dataSource;
    @Resource
    StatisticsMetaMapper statisticsMetaMapper;
    @Resource
    StatisticsRecordMapper statisticsRecordMapper;
    @Resource
    QueryStatisticsRecordDao queryStatisticsRecordDao;

    @Override
    protected BaseMapper getMasterMapper() {
        return statisticsFieldMapper;
    }

    /**
     * 通过域名获取域数据
     */
    @Override
    public StatisticsField getStatisticFieldByName(String field) {
     /*   StatisticsField queryItem = new StatisticsField();
        queryItem.setField(field);*/
        QueryWrapper<StatisticsField> queryItem = new QueryWrapper<StatisticsField>().eq("field", field);
        return statisticsFieldMapper.selectOne(queryItem);
    }

    @Override
    public StatisticsField getStatisticsFieldModel(String field, String identifier) {
        StatisticsField statisticsField = getStatisticFieldByName(field);
        if (statisticsField == null) {
            throw new BusinessException(BusinessCode.BadRequest);
        }

        StatisticsFieldModel model = CRUD.castObject(statisticsField, StatisticsFieldModel.class);


        /// query meta
        List<StatisticsMeta> metas = statisticsMetaMapper.selectList
                (new QueryWrapper<StatisticsMeta>().eq(StatisticsMeta.FIELD, field));
        if (metas != null && !metas.isEmpty()) {

            model.setMetas(metas);

            /// 如果需要实时查询
            if (statisticsField.getAttrRuntime() > 0) {
                for (StatisticsMeta meta: metas) {
                    String sql = meta.getQuerySql();
                    if (sql != null && sql.length() > 0) {
                        List<StatisticsRecord> records = queryStatisticsRecordDao.querySql(sql);
                        records = records == null ? new ArrayList<>() : records;
                        if (identifier != null) {
                            records = records.stream().filter(record -> identifier.equals(record.getIdentifier())).collect(Collectors.toList());
                        }
                        model.addAll(records);
                    }
                };
                // return model;
            }
        }

        // query items
        /*Wrapper<StatisticsRecord> wrapper = new EntityWrapper<StatisticsRecord>().eq("field", field);
        if(identifier != null && !"".equals(identifier)) {
            wrapper.eq("identifier", identifier);
        }

        if (model.getChart().equals("LineTimeline")){
                wrapper.eq("month(create_time)","month(curdate())" )
                        .eq("year(create_time)","year(curdate())");
                wrapper.eq("week(create_time)","week(curdate())" )
                        .eq("year(create_time)","year(curdate())");*//*
        }
        wrapper.orderBy("seq,record_tuple");
        List<StatisticsRecord> items = statisticsRecordMapper.selectList(wrapper);*/
        List<StatisticsRecord> items = queryStatisticsRecordDao.items(field, identifier);
        model.addAll(items);
        return model;
        //WHERE month([column-name]) = month(curdate()) and year([column-name]) = year(curdate())
        //WHERE week([column-name]) = week(curdate()) and year([column-name]) = year(curdate())
        //wrapper.eq("week(create_time)","week(curdate())" );
        /*if (model.getChart().equals("LineTimeline")){
            if (field.equals("total:curmon:count@stat:order:count")){
                wrapper.eq("month(create_time)","month(curdate())" )
                        .eq("year(create_time)","year(curdate())");
            }else if (field.equals("total:curweek:count@stat:order:count"))
            wrapper.eq("week(create_time)","week(curdate())" )
                    .eq("year(create_time)","year(curdate())");
        }*/
        /*if (field.equals("total:curweek:count@stat:order:count")){
            wrapper.eq("week(create_time)","week(curdate())" )
                    .eq("year(create_time)","year(curdate())");
        }else {
            wrapper.eq("month(create_time)","month(curdate())" )
                    .eq("year(create_time)","year(curdate())");
        }*/
    }
}
