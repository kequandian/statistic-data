package com.jfeat.am.module.statistics.services.domain.dao;

import com.jfeat.am.module.statistics.services.domain.model.StatisticsMetaRecord;

import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import com.jfeat.am.module.statistics.services.gen.persistence.model.StatisticsMeta;

import java.util.Date;
import java.util.List;

/**
 * Created by Code Generator on 2020-04-17
 */
public interface QueryStatisticsMetaDao extends BaseMapper<StatisticsMeta> {
    List<StatisticsMetaRecord> findStatisticsMetaPage(Page<StatisticsMetaRecord> page, @Param("record") StatisticsMetaRecord record,
                                            @Param("search") String search, @Param("orderBy") String orderBy,
                                            @Param("startTime") Date startTime, @Param("endTime") Date endTime);
}