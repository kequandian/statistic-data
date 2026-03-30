package com.jfeat.am.module.statistics.services.crud.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jfeat.am.module.statistics.services.persistence.dao.StatisticsGroupMapper;
import com.jfeat.am.module.statistics.services.persistence.model.StatisticsGroup;
import com.jfeat.am.module.statistics.services.crud.StatisticsGroupService;
import com.jfeat.crud.base.exception.BusinessCode;
import com.jfeat.crud.base.exception.BusinessException;
import com.jfeat.crud.plus.impl.CRUDServiceGroupImpl;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * <p>
 *  implementation
 * </p>
 *
 * @author Code Generator
 * @since 2017-11-25
 */
@Service
public class StatisticsGroupServiceImpl  extends CRUDServiceGroupImpl<StatisticsGroup> implements StatisticsGroupService {

    @Resource
    private StatisticsGroupMapper statisticsGroupMapper;

    @Override
    protected BaseMapper<StatisticsGroup> getGroupMapper() {
        return statisticsGroupMapper;
    }

    @Override
    public StatisticsGroup getGroupByName(String name) {
        List<StatisticsGroup> list = statisticsGroupMapper.selectList(
                new QueryWrapper<StatisticsGroup>()
                .eq("name", name));
        if(list==null || list.size()==0){
            return null;
        }
        if(list.size()>1){
            throw new BusinessException(BusinessCode.CRUD_SLAVE_KEY_NOT_PROVIDED);
        }
        return list.get(0);
    }
}


