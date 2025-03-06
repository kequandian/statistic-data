package com.jfeat.am.module.statistics.api;

import com.jfeat.am.module.statistics.services.converter.StatisticConverter;
import com.jfeat.am.module.statistics.services.converter.StatisticGroupData;
import com.jfeat.am.module.statistics.services.converter.StatisticData;
import com.jfeat.am.module.statistics.services.crud.StatisticsFieldService;
import com.jfeat.am.module.statistics.services.crud.StatisticsGroupByService;
import com.jfeat.am.module.statistics.services.crud.StatisticsGroupService;
import com.jfeat.am.module.statistics.services.crud.StatisticsMetaService;
import com.jfeat.am.module.statistics.services.crud.model.StatisticsFieldModel;
import com.jfeat.am.module.statistics.services.crud.model.StatisticsGroupModel;
import com.jfeat.am.module.statistics.services.domain.dao.QueryStatisticsRecordDao;
import com.jfeat.am.module.statistics.services.persistence.model.StatisticsField;
import com.jfeat.am.module.statistics.services.persistence.model.StatisticsGroup;
import com.jfeat.crud.base.exception.BusinessCode;
import com.jfeat.crud.base.exception.BusinessException;
import com.jfeat.crud.base.tips.SuccessTip;
import com.jfeat.crud.base.tips.Tip;
import com.jfeat.crud.plus.CRUD;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


/**
 * <p>
 * api
 * </p>
 *
 * @author Code Generator
 * @since 2017-11-25
 */
@Api("统计 [Statistics]")
@RestController
@RequestMapping("/api/adm/stat/groups")
public class StatisticsGroupEndpoint{
    @Resource
    StatisticsGroupService statisticsGroupService;
    @Resource
    StatisticsGroupByService statisticsGroupByService;
    @Resource
    StatisticsFieldService statisticsFieldService;
    @Resource
    StatisticsMetaService statisticsMetaService;
    @Resource
    QueryStatisticsRecordDao queryStatisticsRecordDao;

    @ApiOperation("获取指定分组的统计数据")
    @GetMapping("/{group}")
    public Tip getStatisticFieldStatisticByGroup(@PathVariable String group,
                                                 @RequestParam(name = "identifier", required = false) String identifier) {
        StatisticsGroup statisticsGroup = statisticsGroupService.getGroupByName(group);
        if (statisticsGroup == null) {
            throw new BusinessException(BusinessCode.BadRequest.getCode(), "statisticsGroup = " + statisticsGroup);
        }
        /// handle specific group
        StatisticGroupData groupData = handleGroupStatistic(statisticsGroup, identifier);

        /// handle sub groups
        List<StatisticsGroup> subgroups = statisticsGroupService.getGroupChildren(statisticsGroup.getId());

        if(subgroups.size()>0){
            // sort sub group first
            if(subgroups.size()>1) {
                subgroups = subgroups.stream().sorted(Comparator.comparing(StatisticsGroup::getIdx)).collect(Collectors.toList());
            }

            for (StatisticsGroup g : subgroups){
                StatisticGroupData gdata = handleGroupStatistic(g, identifier);
                groupData.addItem(gdata);
            }
        }

        return SuccessTip.create(groupData);
    }

    private StatisticGroupData handleGroupStatistic(StatisticsGroup statisticsGroup, String identifier){
        StatisticGroupData groupData = new StatisticGroupData();
            groupData.setGroup(statisticsGroup.getName());
            groupData.setTitle(statisticsGroup.getTitle());
            groupData.setLayout(statisticsGroup.getLayout());
            groupData.setSpan(statisticsGroup.getSpan());
            groupData.setIndex(statisticsGroup.getIdx());

        //每个域的数据
        List<StatisticsField> fields = statisticsGroupByService.getGroupItems(statisticsGroup.getId());

        //过虑不可见
        fields = fields.stream().filter(u->u.getAttrInvisible()==0).collect(Collectors.toList());
        /// 排序
        fields = fields.stream().sorted(Comparator.comparing(StatisticsField::getAttrIndex)).collect(Collectors.toList());

        if(fields.size()>0) {
            for (StatisticsField field : fields) {
                if (!StatisticData.checkStatisticPattern(field.getPattern())) {
                    throw new BusinessException(BusinessCode.BadRequest.getCode(), "统计数据模式错误: Select One in [Count,Rate,Tuple or combine with [Timeline,Cluster]] :" + field.getPattern());
                }

                String fieldName = field.getField();
                Integer isRuntime = field.getAttrRuntime();

                StatisticsFieldModel statisticsField = (StatisticsFieldModel) statisticsFieldService.getStatisticsFieldModel(fieldName, identifier);
                String pattern = field.getPattern();
                StatisticData statisticData = convertStatisticFieldModel(statisticsField, pattern);

                // add item
                groupData.addItem(statisticData);
            }
        }
        return groupData;
    }

    private StatisticData convertStatisticFieldModel(StatisticsFieldModel fieldModel, String pattern) {

        StatisticData statistic = null;

        /// Count pattern
        if (StatisticData.STAT_PATTERN_COUNT.equals(pattern)) {
            statistic = StatisticConverter.convertStatisticCount(fieldModel);
        }
        else if (StatisticData.STAT_PATTERN_COUNT_TIMELINE.equals(pattern)) {
            statistic = StatisticConverter.convertStatisticCountTimeline(fieldModel);
        }
        else if (StatisticData.STAT_PATTERN_COUNT_CLUSTER.equals(pattern)) {
            statistic = StatisticConverter.convertStatisticCountCluster(fieldModel);
        }
        else if (StatisticData.STAT_PATTERN_COUNT_TIMELINE_CLUSTER.equals(pattern)) {
            statistic = StatisticConverter.convertStatisticCountTimelineCluster(fieldModel);
        }

        /// Rate pattern
        else if (StatisticData.STAT_PATTERN_RATE.equals(pattern)) {
            statistic = StatisticConverter.convertStatisticRate(fieldModel);
        }
        else if (StatisticData.STAT_PATTERN_RATE_TIMELINE.equals(pattern)) {
            statistic = StatisticConverter.convertStatisticRateTimeline(fieldModel);
        }
        else if (StatisticData.STAT_PATTERN_RATE_CLUSTER.equals(pattern)) {
            statistic = StatisticConverter.convertStatisticRateCluster(fieldModel);
        }
        else if (StatisticData.STAT_PATTERN_RATE_TIMELINE_CLUSTER.equals(pattern)) {
            statistic = StatisticConverter.convertStatisticRateTimelineCluster(fieldModel);
        }

        /// Tuple pattern
        else if (StatisticData.STAT_PATTERN_TUPLE.equals(pattern)) {
            statistic = StatisticConverter.convertStatisticTuple(fieldModel);
        }
        else if (StatisticData.STAT_PATTERN_TUPLE_TIMELINE.equals(pattern)) {
            statistic = StatisticConverter.convertStatisticTupleTimeline(fieldModel);
        }
        else if (StatisticData.STAT_PATTERN_TUPLE_CLUSTER.equals(pattern)) {
            statistic = StatisticConverter.convertStatisticTupleCluster(fieldModel);
        }
        else if (StatisticData.STAT_PATTERN_TUPLE_TIMELINE_CLUSTER.equals(pattern)) {
            statistic = StatisticConverter.convertStatisticTupleTimelineCluster(fieldModel);
        }

        return statistic;
    }


    @ApiOperation("获取指定分组的统计域(原始数据)")
    @GetMapping("/{group}/origin")
    @Deprecated
    public Tip getStatisticFieldByGroup(@PathVariable String group,
                                        @RequestParam(value = "identifier", required = false) String identifier) {
        StatisticsGroup statisticsGroup = statisticsGroupService.getGroupByName(group);
        if (statisticsGroup == null) {
            throw new BusinessException(BusinessCode.BadRequest);
        }

        StatisticsGroupModel groupModel = CRUD.castObject(statisticsGroup, StatisticsGroupModel.class);

        List<StatisticsField> fields = statisticsGroupByService.getGroupItems(statisticsGroup.getId());

        //过虑不可见
        fields = fields.stream().filter(u->u.getAttrInvisible()==0).collect(Collectors.toList());
        //排序
        fields = fields.stream().sorted(Comparator.comparing(StatisticsField::getAttrIndex)).collect(Collectors.toList());

        //每个域的数据
        List<StatisticsField> modelFields = new ArrayList<>();

        for(StatisticsField field : fields) {
            String fieldName = field.getField();
            StatisticsFieldModel statisticsField =
                    (StatisticsFieldModel) statisticsFieldService.getStatisticsFieldModel(fieldName, identifier);

            modelFields.add(statisticsField);
        }

        groupModel.setFields(modelFields);

        return SuccessTip.create(groupModel);
    }
}