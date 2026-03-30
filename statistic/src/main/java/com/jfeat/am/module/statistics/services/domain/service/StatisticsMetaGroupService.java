package com.jfeat.am.module.statistics.services.domain.service;

import com.alibaba.fastjson2.JSONObject;
import com.jfeat.am.module.statistics.api.model.MetaGroupTemplate;
import com.jfeat.am.module.statistics.api.model.MetaTag;
import com.jfeat.am.module.statistics.api.model.TemplateChildren;
import com.jfeat.am.module.statistics.services.gen.crud.service.CRUDStatisticsMetaGroupService;

/**
 * Created by vincent on 2017/10/19.
 */
public interface StatisticsMetaGroupService extends CRUDStatisticsMetaGroupService {

    JSONObject getTemplateByName(String groupName,MetaTag metaTag);

    JSONObject putChindrenJSON(JSONObject template, TemplateChildren children, String apiReturn, MetaTag metaTag);
}