package com.jfeat.am.module.statistics.services.gen.crud.service.impl;
// ServiceImpl start


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jfeat.crud.plus.FIELD;
import com.jfeat.am.module.statistics.services.gen.persistence.model.StatisticsMetaGroup;
import com.jfeat.am.module.statistics.services.gen.persistence.dao.StatisticsMetaGroupMapper;
import com.jfeat.am.module.statistics.services.gen.crud.service.CRUDStatisticsMetaGroupService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import com.jfeat.crud.base.exception.BusinessCode;
import com.jfeat.crud.base.exception.BusinessException;

import jakarta.annotation.Resource;

import com.jfeat.crud.plus.impl.CRUDServiceOnlyImpl;

/**
 * <p>
 * implementation
 * </p>
 * CRUDStatisticsMetaGroupService
 *
 * @author Code generator
 * @since 2020-10-27
 */

@Service
public class CRUDStatisticsMetaGroupServiceImpl extends CRUDServiceOnlyImpl<StatisticsMetaGroup> implements CRUDStatisticsMetaGroupService {


    @Resource
    protected StatisticsMetaGroupMapper statisticsMetaGroupMapper;

    @Override
    protected BaseMapper<StatisticsMetaGroup> getMasterMapper() {
        return statisticsMetaGroupMapper;
    }


}


