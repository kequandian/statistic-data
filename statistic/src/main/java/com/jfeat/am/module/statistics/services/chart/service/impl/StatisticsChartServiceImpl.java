package com.jfeat.am.module.statistics.services.chart.service.impl;

import com.google.common.collect.Lists;
import com.jfeat.am.module.statistics.services.chart.model.BarChartData;
import com.jfeat.am.module.statistics.services.chart.model.LineChartData;
import com.jfeat.am.module.statistics.services.chart.model.PieChartData;
import com.jfeat.am.module.statistics.services.chart.service.StatisticsChartService;
import com.jfeat.am.module.statistics.services.crud.StatisticsFieldService;
import com.jfeat.am.module.statistics.services.converter.StatisticConverter;
import com.jfeat.am.module.statistics.services.converter.statistic.StatisticDataNameValue;
import com.jfeat.am.module.statistics.services.converter.statistic.StatisticDataRate;
import com.jfeat.am.module.statistics.services.crud.model.StatisticsFieldModel;
import com.jfeat.am.module.statistics.services.persistence.model.StatisticsField;
import com.jfeat.am.module.statistics.services.persistence.model.StatisticsRecord;
import com.jfeat.crud.base.exception.BusinessCode;
import com.jfeat.crud.base.exception.BusinessException;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Silent-Y on 2017/12/4.
 */
@Service
public class StatisticsChartServiceImpl implements StatisticsChartService {

    @Resource
    private StatisticsFieldService statisticsFieldService;

    /*饼状图数据结构
    "title":"饼状图数据结构",
    "timestamp":"数据生成时间",
    "type":"数据图描述类型",
    "data":[
    {value:335, name:'直接访问'},
    {value:310, name:'邮件营销'},
    {value:274, name:'联盟广告'},
    {value:235, name:'视频广告'},
    {value:400, name:'搜索引擎'}
    ]*/
    @Override
    public PieChartData getPieData(String field){
        PieChartData pieChartBean = new PieChartData();
        pieChartBean.setData(new ArrayList<>());

        StatisticsFieldModel fieldModel = (StatisticsFieldModel) statisticsFieldService.getStatisticsFieldModel(field, null);
        if(fieldModel.getAttrInvisible()==1){
            throw new BusinessException(BusinessCode.BadRequest.getCode(), "Current field is invisible");
        }
        if(fieldModel.getItems()==null){
            throw new BusinessException(BusinessCode.BadRequest.getCode(), "No rate data");
        }

        StatisticDataRate statisticDataRate = StatisticConverter.convertStatisticRate(fieldModel);

        pieChartBean.setTitle(statisticDataRate.getName());

        final String PATTERN = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat fmt = new SimpleDateFormat(PATTERN);
        pieChartBean.setTimestamp(fmt.format(new Date()));

        /// convert data
        List<PieChartData.KeyValue> data = pieChartBean.getData();
        for (StatisticDataNameValue rate : statisticDataRate.getRates()) {

            PieChartData.KeyValue keyValue = new PieChartData.KeyValue();
            keyValue.setName(rate.getName());
            keyValue.setValue(rate.getValue());

            data.add(keyValue);
        }

        return pieChartBean;
    }


    /*折线图数据结构
    "title":"折线图数据结构",
    "timestamp":"数据生成时间",
    "type":"数据图描述类型",
    "dataAxis":['点', '击', '柱', '子', '或', '者', '两', '指', '在', '触', '屏', '上', '滑', '动', '能', '够', '自', '动', '缩', '放'];
    "data":[220, 182, 191, 234, 290, 330, 310, 123, 442, 321, 90, 149, 210, 122, 133, 334, 198, 123, 125, 220];*/
    @Override
    public LineChartData getLineData(String field) {
        LineChartData lineChartBean = new LineChartData();
        lineChartBean.setData(new ArrayList<>());
        lineChartBean.setDataAxis(new ArrayList<>());

        StatisticsFieldModel fieldModel = (StatisticsFieldModel) statisticsFieldService.getStatisticsFieldModel(field, null);
        if(fieldModel.getAttrInvisible()==1){
            throw new BusinessException(BusinessCode.BadRequest.getCode(), "Current field is invisible");
        }

        lineChartBean.setTitle(fieldModel.getName());
        final String PATTERN = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat fmt = new SimpleDateFormat(PATTERN);
        lineChartBean.setTimestamp(fmt.format(new Date()));

        if(fieldModel.getItems()==null){
            fieldModel.setItems(new ArrayList<>());
        }

        /// convert to chart data
        List<String> data = lineChartBean.getData();
        List<String> dataAxis = lineChartBean.getDataAxis();
        for (StatisticsRecord record : fieldModel.getItems()) {
            data.add(record.getRecordValue());
            dataAxis.add(record.getRecordName());
        }

        return lineChartBean;
    }



    /*柱状图数据结构
    "title":"柱状图数据结构",
    "timestamp":"数据生成时间",
    "type":"数据图描述类型",
    "dataAxis":['点', '击', '柱', '子', '或', '者', '两', '指', '在', '触', '屏', '上', '滑', '动', '能', '够', '自', '动', '缩', '放'];
    "data":[220, 182, 191, 234, 290, 330, 310, 123, 442, 321, 90, 149, 210, 122, 133, 334, 198, 123, 125, 220];*/
    @Override
    public BarChartData getBarData(String field) {
        BarChartData barChartBean = new BarChartData();
        barChartBean.setData(new ArrayList<>());
        barChartBean.setDataAxis(new ArrayList<>());

        StatisticsFieldModel fieldModel = (StatisticsFieldModel) statisticsFieldService.getStatisticsFieldModel(field, null);
        if(fieldModel.getAttrInvisible()==1){
            throw new BusinessException(BusinessCode.BadRequest.getCode(), "Current field is invisible");
        }
        barChartBean.setTitle(fieldModel.getName());

        final String PATTERN = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat fmt = new SimpleDateFormat(PATTERN);
        barChartBean.setTimestamp(fmt.format(new Date()));

        if(fieldModel.getItems()==null){
            fieldModel.setItems(new ArrayList<>());
        }

        /// convert to chart data
        List<String> data = barChartBean.getData();
        List<String> dataAxis = barChartBean.getData();
        for (StatisticsRecord record : fieldModel.getItems()) {
            data.add(record.getRecordValue());
            dataAxis.add(record.getRecordName());
        }

        return barChartBean;
    }



    /*折线图数据结构
        "title":"折线图",
        "timestamp":"数据生成时间",
        "type":"数据图描述类型[line]",
        "dataAxis":['点', '击', '柱', '子', '或', '者', '两', '指', '在', '触', '屏', '上', '滑', '动', '能', '够', '自', '动', '缩', '放'];
        "data":[220, 182, 191, 234, 290, 330, 310, 123, 442, 321, 90, 149, 210, 122, 133, 334, 198, 123, 125, 220];
    */
    @Deprecated
    public Map<String, Object> getStatisticLineData(String field, String startTime, String endTime) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        //获取前月的第一天
        Calendar cal_1=Calendar.getInstance();//获取当前日期
        cal_1.add(Calendar.MONTH, -1);
        cal_1.set(Calendar.DAY_OF_MONTH,1);//设置为1号,当前日期既为本月第一天
        String firstDay = format.format(cal_1.getTime());
        //获取前月的最后一天
        Calendar cale = Calendar.getInstance();
        cale.set(Calendar.DAY_OF_MONTH,0);//设置为1号,当前日期既为本月第一天
        String lastDay = format.format(cale.getTime());
        if (startTime == null){
            startTime = firstDay;
        }
        if (endTime == null){
            endTime = lastDay;
        }

        StatisticsField statisticsField = statisticsFieldService.getStatisticFieldByName(field);
        if(statisticsField==null){
            throw new BusinessException(BusinessCode.BadRequest.getCode(), "Invalid field name: " + field);
        }

        ///
        LineChartData lineChartBean = new LineChartData();
        List<String> dataAxis = Lists.newArrayList();
        List<String> data = Lists.newArrayList();

        lineChartBean.setTitle(statisticsField.getName());
        final String PATTERN = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat fmt = new SimpleDateFormat(PATTERN);
        lineChartBean.setTimestamp(fmt.format(new Date()));
        lineChartBean.setDataAxis(dataAxis);
        lineChartBean.setData(data);

        return null;
    }
}
