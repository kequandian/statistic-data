package com.jfeat.am.module.statistics.api;

import com.jfeat.am.module.statistics.services.crud.StatisticsFieldService;
import com.jfeat.am.module.statistics.services.converter.StatisticConverter;
import com.jfeat.am.module.statistics.services.converter.StatisticData;
import com.jfeat.am.module.statistics.services.crud.model.StatisticsFieldModel;
import com.jfeat.am.module.statistics.services.persistence.model.StatisticsField;
import com.jfeat.crud.base.exception.BusinessCode;
import com.jfeat.crud.base.exception.BusinessException;
import com.jfeat.crud.base.tips.SuccessTip;
import com.jfeat.crud.base.tips.Tip;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * Created by Silent-Y on 2017/10/16.
 */
@Deprecated //原则上无需单独获取域数据
@Api("统计 [Statistics]")
@RestController
@RequestMapping("/api/adm/stat/fields")
public class StatisticsFieldEndpoint{

    @Resource
    StatisticsFieldService statisticsFieldService;

    //@Resource
    //GeneralStatisticService generalStatisticService;

    @ApiOperation("获取指定数据域数据")
    @GetMapping("/{field}")
    public Tip getStatisticFieldRaw(@PathVariable String field) {
        StatisticsField statisticsField = statisticsFieldService.getStatisticsFieldModel(field, null);
        return SuccessTip.create(statisticsField);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "[total,rate,tuple,totalTimeline,rateTimeline,tupleTimeline]", paramType = "query", dataType = "string")
    })
    @ApiOperation("获取指定数据域数据")
    @GetMapping("/{field}/statistic")
    public Tip getStatisticField(@PathVariable String field) {
        StatisticsField statisticsField = statisticsFieldService.getStatisticsFieldModel(field, null);

        String pattern = statisticsField.getPattern();

        Object statistic = null;

        if (statisticsField instanceof StatisticsFieldModel) {
            StatisticsFieldModel fieldModel = (StatisticsFieldModel) statisticsField;

            // pattern count
            if (StatisticData.STAT_PATTERN_COUNT.equals(pattern)) {
                statistic = StatisticConverter.convertStatisticCount(fieldModel);
            }
            if (StatisticData.STAT_PATTERN_COUNT_TIMELINE.equals(pattern)) {
                statistic = StatisticConverter.convertStatisticCountTimeline(fieldModel);
            }
            if (StatisticData.STAT_PATTERN_COUNT_CLUSTER.equals(pattern)) {
                statistic = StatisticConverter.convertStatisticCountCluster(fieldModel);
            }
            if (StatisticData.STAT_PATTERN_COUNT_TIMELINE_CLUSTER.equals(pattern)) {
                statistic = StatisticConverter.convertStatisticCountTimelineCluster(fieldModel);
            }

            //pattern rate
            if (StatisticData.STAT_PATTERN_RATE.equals(pattern)) {
                statistic = StatisticConverter.convertStatisticRate(fieldModel);
            }
            if (StatisticData.STAT_PATTERN_RATE_TIMELINE.equals(pattern)) {
                statistic = StatisticConverter.convertStatisticRateTimeline(fieldModel);
            }
            if (StatisticData.STAT_PATTERN_RATE_CLUSTER.equals(pattern)) {
                statistic = StatisticConverter.convertStatisticRateCluster(fieldModel);
            }
            if (StatisticData.STAT_PATTERN_RATE_TIMELINE_CLUSTER.equals(pattern)) {
                statistic = StatisticConverter.convertStatisticRateTimelineCluster(fieldModel);
            }

            //pattern tuple
            if (StatisticData.STAT_PATTERN_TUPLE.equals(pattern)) {
                statistic = StatisticConverter.convertStatisticTuple(fieldModel);
            }
            if (StatisticData.STAT_PATTERN_TUPLE_TIMELINE.equals(pattern)) {
                statistic = StatisticConverter.convertStatisticTupleTimeline(fieldModel);
            }
            if (StatisticData.STAT_PATTERN_TUPLE_CLUSTER.equals(pattern)) {
                statistic = StatisticConverter.convertStatisticTupleCluster(fieldModel);
            }
            if (StatisticData.STAT_PATTERN_TUPLE_TIMELINE_CLUSTER.equals(pattern)) {
                statistic = StatisticConverter.convertStatisticTupleTimelineCluster(fieldModel);
            }

        }//else if(statisticsField.getQuerySql()!=null){
        //TODO, 考虑StatisticData 与 statement 中的 StatisticRate 区别
        //}
        else {
            throw new BusinessException(BusinessCode.BadRequest.getCode(), "无效的统计域，请检查统计域的querySql字段");
        }

        return SuccessTip.create(statistic);
    }
}
