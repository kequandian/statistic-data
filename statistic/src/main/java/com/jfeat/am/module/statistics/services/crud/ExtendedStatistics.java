package com.jfeat.am.module.statistics.services.crud;

import com.alibaba.fastjson2.JSONObject;
import com.jfeat.am.module.statistics.api.model.MetaGroupTemplate;
import com.jfeat.am.module.statistics.api.model.MetaTag;
import com.jfeat.am.module.statistics.api.model.TemplateChildren;
import com.jfeat.am.module.statistics.services.crud.model.ReportCode;
import com.jfeat.am.module.statistics.services.gen.persistence.model.StatisticsMeta;

public interface ExtendedStatistics {

    JSONObject getByPattern(StatisticsMeta statisticsMetas, MetaTag metaTag);

    Integer recordHistory(String field, String appid);

    String genCode(ReportCode reportCode);


    JSONObject getReportByCode(ReportCode reportCode, MetaTag metaTag);

    //根据id获取表单信息
    JSONObject getJSONById(Long id, MetaTag metaTag);

    //根据Field获取表单信息
    JSONObject getJSONByField(String field, MetaTag metaTag);

    JSONObject getCountTemplate(String field);

    JSONObject getRateTemplate(String field);

    JSONObject getTimeLineTemplate(String field);
}
