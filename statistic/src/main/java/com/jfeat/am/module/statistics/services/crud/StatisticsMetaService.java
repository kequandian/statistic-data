package com.jfeat.am.module.statistics.services.crud;

import com.alibaba.fastjson2.JSONObject;
import com.jfeat.am.module.statistics.api.model.MetaTag;
import com.jfeat.am.module.statistics.services.domain.model.StatisticsMetaRecord;
import com.jfeat.am.module.statistics.services.gen.crud.service.CRUDStatisticsMetaService;
import com.jfeat.am.module.statistics.services.gen.persistence.model.StatisticsMeta;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>
 * service interface
 * </p>
 *
 * @author Code Generator
 * @since 2018-07-29
 * Master: ${cfg.masterModel}
 * Slave : st_statistics_meta
 */
public interface StatisticsMetaService extends CRUDStatisticsMetaService {


    StatisticsMeta getStatisticsMetaById(Long id);


    String getQuerySql(String field, String recordName, String tuple, String cluster, String timeline);

    //根据field获取 json化的 表
    //public JSONObject getByField(String field, Long current, Long  size, HttpServletRequest request);

    //根据字段获取配置
    StatisticsMeta getStatisticsMetas(String field);

    //根据field获取 json化的 表
    JSONObject getByField(String field, MetaTag metaTag);


    //countSQL 用于查询总记录数
    JSONObject getTableInfo(JSONObject data, String[] typeArray, StringBuilder sql, MetaTag metaTag, String rowsName);

    //根据field和条件 获取sql
    public String getSQLByField(String field, HttpServletRequest request);

    //根据field来动态改变json文件
    public JSONObject getOutputSetting(String field,String title);
}
