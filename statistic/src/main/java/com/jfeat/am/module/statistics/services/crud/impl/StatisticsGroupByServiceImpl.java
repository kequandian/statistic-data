package com.jfeat.am.module.statistics.services.crud.impl;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jfeat.am.module.statistics.services.persistence.dao.StatisticsFieldMapper;
import com.jfeat.am.module.statistics.services.persistence.dao.StatisticsGroupMapper;
import com.jfeat.am.module.statistics.services.persistence.model.StatisticsField;
import com.jfeat.am.module.statistics.services.persistence.model.StatisticsGroup;
import com.jfeat.am.module.statistics.services.crud.StatisticsGroupByService;
import com.jfeat.crud.plus.impl.CRUDServiceGroupByImpl;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

/**
 * <p>
 *  implementation
 * </p>
 *
 * @author Code Generator
 * @since 2017-11-25
 */
@Service
public class StatisticsGroupByServiceImpl extends CRUDServiceGroupByImpl<StatisticsGroup, StatisticsField>
        implements StatisticsGroupByService {

    static final String groupById = "group_id";

    @Resource
    private StatisticsGroupMapper statisticsGroupMapper;

    @Resource
    private StatisticsFieldMapper statisticsFieldMapper;

    @Override
    protected BaseMapper<StatisticsGroup> getGroupMapper() {
        return statisticsGroupMapper;
    }

    @Override
    protected BaseMapper<StatisticsField> getGroupByMapper() {
        return statisticsFieldMapper;
    }

    @Override
    protected String groupByFieldName() {
        return groupById;
    }
}


