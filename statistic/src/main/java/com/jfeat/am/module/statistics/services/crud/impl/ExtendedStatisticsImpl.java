package com.jfeat.am.module.statistics.services.crud.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jfeat.am.module.statistics.api.model.MetaGroupTemplate;
import com.jfeat.am.module.statistics.api.model.MetaTag;
import com.jfeat.am.module.statistics.api.model.TemplateChildren;
import com.jfeat.am.module.statistics.services.crud.ExtendedStatistics;
import com.jfeat.am.module.statistics.services.crud.StatisticsMetaService;
import com.jfeat.am.module.statistics.services.gen.persistence.model.StatisticsMeta;
import com.jfeat.crud.base.exception.BusinessCode;
import com.jfeat.crud.base.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ExtendedStatisticsImpl implements ExtendedStatistics {

    private static final Logger logger = LoggerFactory.getLogger(ExtendedStatisticsImpl.class);

    @Resource
    StatisticsMetaService statisticsMetaService;

    //根据id获取表单信息
    @Override
    public JSONObject getJSONById(Long id, MetaTag metaTag){
        StatisticsMeta statisticsMetas = statisticsMetaService.getStatisticsMetaById(id);
        return getByPattern(statisticsMetas,metaTag);
    }

    //根据Field获取表单信息
    @Override
    public JSONObject getJSONByField(String field, MetaTag metaTag){
        StatisticsMeta statisticsMetas = statisticsMetaService.getStatisticsMetas(field);
        return getByPattern(statisticsMetas,metaTag);
    }

    @Override
    public JSONObject getByPattern(StatisticsMeta statisticsMetas,MetaTag metaTag){
        JSONObject data = new JSONObject();
        String field = statisticsMetas.getField();
        String pattern = statisticsMetas.getPattern();
        if(StringUtils.isEmpty(pattern)){
            metaTag.setEnableHead(true);
            metaTag.setEnablePages(true);
            metaTag.setEnableSearch(true);
            metaTag.setEnableTips(true);
            metaTag.setEnableType(true);
            data=statisticsMetaService.getByField(field,metaTag);
        }else{
            switch (pattern){
                case "count":case "Count": data=this.getCountTemplate(field);break;
                case "Rate": case "rate" : data=this.getRateTemplate(field);break;
                case "Timeline":case "TimeLine": case "timeLine": case "timeline": data=this.getTimeLineTemplate(field);break;
                default : logger.info("当前配置类型:{} 配置的域:{}",pattern,field);throw new BusinessException(BusinessCode.ErrorStatus,"该类型未配置");
            }
        }
        if(!StringUtils.isEmpty(statisticsMetas.getIcon())){
            data.put("icon",analysisIcon(statisticsMetas.getIcon()));
        }
        if(!StringUtils.isEmpty(statisticsMetas.getSpan())){
            data.put("span",statisticsMetas.getSpan());
        }
        if(!StringUtils.isEmpty(statisticsMetas.getTitle())){
            data.put("title",statisticsMetas.getTitle());
        }


        return data;
    }



    @Override
    public JSONObject getCountTemplate(String field){
        MetaTag mataTag= new MetaTag();

        JSONObject data=new JSONObject();
        StatisticsMeta statisticsMetas = statisticsMetaService.getStatisticsMetas(field);
        StringBuilder sql = new StringBuilder(statisticsMetas.getQuerySql());


        data = statisticsMetaService.getTableInfo(data, null, sql, mataTag,"rates");
        return data;
    }

    @Override
    public JSONObject getRateTemplate(String field){
        MetaTag mataTag= new MetaTag();
        JSONObject pie=new JSONObject();

        StatisticsMeta statisticsMetas = statisticsMetaService.getStatisticsMetas(field);
        pie.put("field",field);
        pie.put("pattern","Rote");


        if(StringUtils.isEmpty(statisticsMetas.getChart())){
            pie.put("chart","Pie");
        }else{
            pie.put("chart",statisticsMetas.getChart());
        }
        pie.put("name",statisticsMetas.getTitle());

        StringBuilder sql = new StringBuilder(statisticsMetas.getQuerySql());
        pie = statisticsMetaService.getTableInfo(pie, null, sql, mataTag,"rates");
        return pie;

    }

    @Override
    public JSONObject getTimeLineTemplate(String field){
        MetaTag mataTag= new MetaTag();
        JSONObject timeLine=new JSONObject();

        StatisticsMeta statisticsMetas = statisticsMetaService.getStatisticsMetas(field);

        if(StringUtils.isEmpty(statisticsMetas.getChart())){
            timeLine.put("chart","BarGroup_2");
        }else{
            timeLine.put("chart",statisticsMetas.getChart());
        }

        timeLine.put("field",statisticsMetas.getField());
        timeLine.put("identifier","");
        timeLine.put("name",statisticsMetas.getTitle());
        timeLine.put("pattern","Rate");
        timeLine.put("tl","");

        StringBuilder sql = new StringBuilder(statisticsMetas.getQuerySql());
        timeLine = statisticsMetaService.getTableInfo(timeLine, null, sql, mataTag,"rates");

        return timeLine;

    }

    //解析icon
    public JSONArray analysisIcon(String icon){
        JSONArray jsonArray=new JSONArray();
        String[] split = icon.split(",");
        for(String s:split){
            JSONObject jsonObject = new JSONObject();
            String[] keyValueArray = s.split("-");
            if(keyValueArray.length!=2){throw new BusinessException(BusinessCode.CRUD_QUERY_FAILURE,"配置异常 配置内容:"+s+"对应格式：字段-[符号]&[符号]");}
            jsonObject.put(keyValueArray[0],keyValueArray[1]);
            jsonArray.add(jsonObject);
        }
        return jsonArray;
    }

}
