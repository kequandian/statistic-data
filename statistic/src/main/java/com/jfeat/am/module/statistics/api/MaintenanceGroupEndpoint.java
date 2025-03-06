package com.jfeat.am.module.statistics.api;

import com.jfeat.am.module.statistics.services.persistence.model.StatisticsGroup;
import com.jfeat.am.module.statistics.services.crud.StatisticsGroupService;
import com.jfeat.crud.base.tips.SuccessTip;
import com.jfeat.crud.base.tips.Tip;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 *  api
 * </p>
 *
 * @author Code Generator
 * @since 2017-10-19
 */
@Api("统计 [Statistics]")
@RestController
@RequestMapping("/api/cfg/stat/groups")
public class MaintenanceGroupEndpoint{

    @Resource
    StatisticsGroupService statisticsGroupService;

    /**
     * maintenance of group
     * @return
     */
    /*@ApiOperation("返回所有组")
    @GetMapping
    public Tip getAllStatisticsGroups(@RequestParam(required = false) String chart) {
        List<StatisticsGroup> groups = statisticsGroupService.getGroupTuples("chart", chart);
        return SuccessTip.create(groups);
    }*/


    @ApiOperation("获取所有组")
    @GetMapping
    public Tip getConfigGroupList() {
        return SuccessTip.create(statisticsGroupService.getGroupTuples());
    }

    @ApiOperation("获取组")
    @GetMapping("/{id}")
    public Tip getConfigGroup(@PathVariable Long id) {
        return SuccessTip.create(statisticsGroupService.retrieveGroup(id));
    }

    @ApiOperation("删除组")
    @DeleteMapping("/{id}")
    public Tip deleteConfigGroup(@PathVariable Long id) {
        return SuccessTip.create(statisticsGroupService.deleteGroup(id));
    }

    @ApiOperation("获取组的子组")
    @GetMapping("/{id}/children")
    public Tip getConfigGroupChildren(@PathVariable Long id) {
        return SuccessTip.create(statisticsGroupService.getGroupChildren(id));
    }

    @ApiOperation(value = "增加组", response = StatisticsGroup.class)
    @PostMapping
    public Tip createConfigGroup(@RequestBody StatisticsGroup entity) {
        return SuccessTip.create(statisticsGroupService.createGroup(entity));
    }

    @ApiOperation(value = "修改组", response = StatisticsGroup.class)
    @PutMapping("/{id}")
    public Tip updateConfigGroupAllColumns(@PathVariable Long id, @RequestBody StatisticsGroup entity) {
        entity.setId(id);
        return SuccessTip.create(statisticsGroupService.updateGroup(entity, true));
    }

    @ApiOperation(value = "修改组（选择具体某项修改）", response = StatisticsGroup.class)
    @PatchMapping("/{id}")
    public Tip updateConfigGroup(@PathVariable Long id, @RequestBody StatisticsGroup entity) {
        entity.setId(id);
        return SuccessTip.create(statisticsGroupService.updateGroup(entity, false));
    }
}
