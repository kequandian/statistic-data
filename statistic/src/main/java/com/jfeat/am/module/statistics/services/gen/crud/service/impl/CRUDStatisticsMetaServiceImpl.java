package com.jfeat.am.module.statistics.services.gen.crud.service.impl;
            
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jfeat.crud.plus.FIELD;
import com.jfeat.am.module.statistics.services.gen.persistence.model.StatisticsMeta;
import com.jfeat.am.module.statistics.services.gen.persistence.dao.StatisticsMetaMapper;
import com.jfeat.am.module.statistics.services.gen.crud.service.CRUDStatisticsMetaService;
import org.springframework.stereotype.Service;
import com.jfeat.crud.base.exception.BusinessCode;
import com.jfeat.crud.base.exception.BusinessException;
import jakarta.annotation.Resource;
import com.jfeat.crud.plus.impl.CRUDServiceOnlyImpl;

/**
 * <p>
 *  implementation
 * </p>
 *CRUDStatisticsMetaService
 * @author Code Generator
 * @since 2020-04-17
 */

@Service
public class CRUDStatisticsMetaServiceImpl  extends CRUDServiceOnlyImpl<StatisticsMeta> implements CRUDStatisticsMetaService {





        @Resource
        protected StatisticsMetaMapper statisticsMetaMapper;

        @Override
        protected BaseMapper<StatisticsMeta> getMasterMapper() {
                return statisticsMetaMapper;
        }







}


