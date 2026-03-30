package com.jfeat.am.module.statistics.services.gen.crud.service.impl;
// ServiceImpl start

            
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jfeat.crud.plus.FIELD;
import com.jfeat.am.module.statistics.services.gen.persistence.model.StatisticsUsedHistory;
import com.jfeat.am.module.statistics.services.gen.persistence.dao.StatisticsUsedHistoryMapper;
import com.jfeat.am.module.statistics.services.gen.crud.service.CRUDStatisticsUsedHistoryService;
import org.springframework.stereotype.Service;
import com.jfeat.crud.base.exception.BusinessCode;
import com.jfeat.crud.base.exception.BusinessException;
import jakarta.annotation.Resource;
import com.jfeat.crud.plus.impl.CRUDServiceOnlyImpl;

/**
 * <p>
 *  implementation
 * </p>
 *CRUDStatisticsUsedHistoryService
 * @author Code generator
 * @since 2022-08-31
 */

@Service
public class CRUDStatisticsUsedHistoryServiceImpl  extends CRUDServiceOnlyImpl<StatisticsUsedHistory> implements CRUDStatisticsUsedHistoryService {





        @Resource
        protected StatisticsUsedHistoryMapper statisticsUsedHistoryMapper;

        @Override
        protected BaseMapper<StatisticsUsedHistory> getMasterMapper() {
                return statisticsUsedHistoryMapper;
        }







}


