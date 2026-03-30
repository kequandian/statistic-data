package com.jfeat.am.module.statistics.services.domain.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jfeat.am.module.statistics.api.model.MetaTag;
import com.jfeat.am.module.statistics.api.model.TemplateChildren;
import com.jfeat.am.module.statistics.services.crud.ExtendedStatistics;
import com.jfeat.am.module.statistics.services.crud.model.ApiReturn;
import com.jfeat.am.module.statistics.services.domain.service.StatisticsMetaGroupService;
import com.jfeat.am.module.statistics.services.gen.crud.service.impl.CRUDStatisticsMetaGroupServiceImpl;
import com.jfeat.am.module.statistics.services.gen.persistence.dao.StatisticsMetaGroupMapper;
import com.jfeat.am.module.statistics.services.gen.persistence.model.StatisticsMetaGroup;
import com.jfeat.crud.base.exception.BusinessCode;
import com.jfeat.crud.base.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author admin
 * @since 2017-10-16
 */

@Service("statisticsMetaGroupService")
public class StatisticsMetaGroupServiceImpl extends CRUDStatisticsMetaGroupServiceImpl implements StatisticsMetaGroupService {

    @Resource
    StatisticsMetaGroupMapper smgMapper;
    @Resource
    ExtendedStatistics extendedStatistics;

    private final static String META_API_URL = "/api/adm/stat/meta/";

    protected final static Logger logger = LoggerFactory.getLogger(StatisticsMetaGroupServiceImpl.class);

    public StatisticsMetaGroup getPGroup(String groupName) {
        StatisticsMetaGroup group = smgMapper.selectOne(new QueryWrapper<StatisticsMetaGroup>()
                .eq("name", groupName));

        if (group == null) {
            throw new BusinessException(BusinessCode.CRUD_QUERY_FAILURE, "查询失败,未找到对应配置");
        }
        return group;
    }

    public JSONObject setHead(JSONObject mgTemplate, StatisticsMetaGroup pGroup) {
        String layout = pGroup.getLayout();
        if (!StringUtils.isEmpty(layout)) {
            JSONObject jsonObject = JSONObject.parseObject(layout);
            mgTemplate.put("layout", jsonObject);
        } else {
            mgTemplate.put("layout", "");
        }

        mgTemplate.put("span", pGroup.getSpan());
        mgTemplate.put("title", pGroup.getTitle());
        return mgTemplate;
    }

    public JSONObject setBody(JSONObject mgTemplate, Long pid,MetaTag metaTag) {
        List<StatisticsMetaGroup> sonList = smgMapper.selectList(new QueryWrapper<StatisticsMetaGroup>().eq("pid", pid));

        List<TemplateChildren> templateChildrenList = new ArrayList<>();

        for (StatisticsMetaGroup statisticsMetaGroup : sonList) {
            if (StringUtils.isEmpty(statisticsMetaGroup.getField())) {
                JSONObject pJSON = setBody(new JSONObject(), statisticsMetaGroup.getId(),metaTag);
                pJSON = setHead(pJSON, statisticsMetaGroup);
                mgTemplate.put(statisticsMetaGroup.getName(), pJSON);
                TemplateChildren templateChildren = new TemplateChildren(statisticsMetaGroup);
                templateChildrenList.add(templateChildren);
            } else {
                //处理子类
                TemplateChildren templateChildren = new TemplateChildren(statisticsMetaGroup);
                templateChildrenList.add(templateChildren);
                putChindrenJSON(mgTemplate, templateChildren, statisticsMetaGroup.getApiReturn(),metaTag);
            }
        }
        //列表排序
        templateChildrenList.sort(Comparator.comparingInt(TemplateChildren::getSeq));
        mgTemplate.put("children", templateChildrenList);

        return mgTemplate;
    }

    @Override
    public JSONObject getTemplateByName(String groupName,MetaTag metaTag) {
        StatisticsMetaGroup pGroup = getPGroup(groupName);
        JSONObject templateJSON = new JSONObject();
        templateJSON = setHead(templateJSON, pGroup);
        templateJSON = setBody(templateJSON, pGroup.getId(),metaTag);
        return templateJSON;
    }


    @Override
    public JSONObject putChindrenJSON(JSONObject template, TemplateChildren children, String apiReturn,MetaTag metaTag) {

        JSONObject chJson = new JSONObject();
        if (ApiReturn.API_JSON.equals(apiReturn.trim())) {
            chJson = extendedStatistics.getJSONByField(children.getField(),metaTag);

        } else {
            StringBuilder sb = new StringBuilder(META_API_URL);
            sb.append(children.getField());
            chJson.put("api",sb.toString());
        }
        template.put(children.getField(), chJson);

        return template;
    }

}
