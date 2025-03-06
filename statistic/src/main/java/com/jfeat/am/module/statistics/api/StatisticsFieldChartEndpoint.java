package com.jfeat.am.module.statistics.api;

import com.jfeat.am.module.statistics.services.chart.service.StatisticsChartService;
import com.jfeat.crud.base.tips.SuccessTip;
import com.jfeat.crud.base.tips.Tip;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * Created by Silent-Y on 2017/10/16.
 */
@Deprecated  //原则上无需单独获取域数据
@Api("统计 [Statistics]")
@RestController
@RequestMapping("/api/adm/stat/fields")
public class StatisticsFieldChartEndpoint{
    @Resource
    StatisticsChartService chartService;

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
    @ApiOperation("获取指定数据域数据 [pie]")
    @GetMapping("/{field}/chart/pie")
    public Tip getPieData(@PathVariable String field) {
        return SuccessTip.create(chartService.getPieData(field));
    }


    /*柱状图数据结构
    "title":"柱状图数据结构",
    "timestamp":"数据生成时间",
    "type":"数据图描述类型",
    "dataAxis":['点', '击', '柱', '子', '或', '者', '两', '指', '在', '触', '屏', '上', '滑', '动', '能', '够', '自', '动', '缩', '放'];
    "data":[220, 182, 191, 234, 290, 330, 310, 123, 442, 321, 90, 149, 210, 122, 133, 334, 198, 123, 125, 220];*/
    @ApiOperation("获取指定数据域数据 [bar]")
    @GetMapping("/{field}/chart/bar")
    public Tip getBarData(@PathVariable String field) {
        return SuccessTip.create(chartService.getBarData(field));
    }


    /*折线图数据结构
    "title":"折线图数据结构",
    "timestamp":"数据生成时间",
    "type":"数据图描述类型",
    "dataAxis":['点', '击', '柱', '子', '或', '者', '两', '指', '在', '触', '屏', '上', '滑', '动', '能', '够', '自', '动', '缩', '放'];
    "data":[220, 182, 191, 234, 290, 330, 310, 123, 442, 321, 90, 149, 210, 122, 133, 334, 198, 123, 125, 220];*/
    @ApiOperation("获取指定数据域数据 [line]")
    @GetMapping("/{field}/chart/line")
    public Tip getLineData(@PathVariable String field) {
        return SuccessTip.create(chartService.getLineData(field));
    }


    /*
    @Resource
    StatisticsGroupService statisticsGroupService;
    @Resource
    StatisticsGroupByService statisticsGroupByService;

    @ApiOperation("获取指定分组标识的数据域组 [pie]")
    @GetMapping("/{group}/chart/pie")
    public Tip getStatisticChartPieByGroup(@PathVariable String group) {
        StatisticsGroup statisticsGroup = statisticsGroupService.getGroupByName(group);
        if (statisticsGroup == null) {
            throw new BusinessException(BusinessCode.BadRequest.getCode(), "Invalid group identifier");
        }

        GroupPieChartData groupPieChartBean = CRUD.castObject(statisticsGroup, GroupPieChartData.class);

        List<StatisticsField> fields = statisticsGroupByService.getGroupItems(statisticsGroup.getId());
        if(fields==null || fields.size()==0){
            throw new BusinessException(BusinessCode.BadRequest.getCode(), "No items in the group");
        }


        List<PieChartData> items = new ArrayList<>();

        for(StatisticsField field : fields){
            PieChartData bean = chartService.getPieData(field.getField());
            items.add(bean);
        }

        groupPieChartBean.setItems(items);

        return SuccessTip.create(groupPieChartBean);
    }

    @ApiOperation("获取指定分组标识的数据域组 [bar]")
    @GetMapping("/{group}/chart/bar")
    public Tip getStatisticChartBarByGroup(@PathVariable String group) {
        StatisticsGroup statisticsGroup = statisticsGroupService.getGroupByName(group);
        if (statisticsGroup == null) {
            throw new BusinessException(BusinessCode.BadRequest.getCode(), "Invalid group identifier");
        }

        GroupBarChartData groupBarChartBean = CRUD.castObject(statisticsGroup, GroupBarChartData.class);

        List<StatisticsField> fields = statisticsGroupByService.getGroupItems(statisticsGroup.getId());
        if(fields==null || fields.size()==0){
            throw new BusinessException(BusinessCode.BadRequest.getCode(), "No items in the group");
        }


        List<BarChartData> items = new ArrayList<>();

        for(StatisticsField field : fields){
            BarChartData bean = chartService.getBarData(field.getField());
            items.add(bean);
        }

        groupBarChartBean.setItems(items);

        return SuccessTip.create(groupBarChartBean);
    }

    @ApiOperation("获取指定分组标识的数据域组 [line]")
    @GetMapping("/{group}/chart/line")
    public Tip getStatisticChartLineByGroup(@PathVariable String group) {
        StatisticsGroup statisticsGroup = statisticsGroupService.getGroupByName(group);
        if (statisticsGroup == null) {
            throw new BusinessException(BusinessCode.BadRequest.getCode(), "Invalid group identifier");
        }

        GroupLineChartData groupLineChartBean = CRUD.castObject(statisticsGroup, GroupLineChartData.class);

        List<StatisticsField> fields = statisticsGroupByService.getGroupItems(statisticsGroup.getId());
        if(fields==null || fields.size()==0){
            throw new BusinessException(BusinessCode.BadRequest.getCode(), "No items in the group");
        }


        List<LineChartData> items = new ArrayList<>();

        for(StatisticsField field : fields){
            LineChartData bean = chartService.getLineData(field.getField());
            items.add(bean);
        }

        groupLineChartBean.setItems(items);

        return SuccessTip.create(groupLineChartBean);
    }*/

}
