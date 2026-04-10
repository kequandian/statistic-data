package com.jfeat.am.module.statistics.api;

import com.jfeat.am.module.statistics.services.persistence.model.StatisticsGroup;
import com.jfeat.am.module.statistics.services.crud.StatisticsGroupService;
import com.jfeat.crud.base.exception.BusinessCode;
import com.jfeat.crud.base.exception.BusinessException;
import com.jfeat.crud.base.tips.SuccessTip;
import com.jfeat.crud.base.tips.Tip;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * <p>
 * Statistics Group Maintenance API
 * Enhanced with validation and error handling
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
        StatisticsGroup group = statisticsGroupService.retrieveGroup(id);
        if (group == null) {
            throw new BusinessException(BusinessCode.CRUD_QUERY_FAILURE, "StatisticsGroup not found with id: " + id);
        }
        return SuccessTip.create(group);
    }

    @ApiOperation("删除组")
    @DeleteMapping("/{id}")
    public Tip deleteConfigGroup(@PathVariable Long id) {
        StatisticsGroup group = statisticsGroupService.retrieveGroup(id);
        if (group == null) {
            throw new BusinessException(BusinessCode.CRUD_QUERY_FAILURE, "StatisticsGroup not found with id: " + id);
        }
        return SuccessTip.create(statisticsGroupService.deleteGroup(id));
    }

    @ApiOperation("获取组的子组")
    @GetMapping("/{id}/children")
    public Tip getConfigGroupChildren(@PathVariable Long id) {
        StatisticsGroup group = statisticsGroupService.retrieveGroup(id);
        if (group == null) {
            throw new BusinessException(BusinessCode.CRUD_QUERY_FAILURE, "StatisticsGroup not found with id: " + id);
        }
        return SuccessTip.create(statisticsGroupService.getGroupChildren(id));
    }

    @ApiOperation(value = "增加组", response = StatisticsGroup.class)
    @PostMapping
    public Tip createConfigGroup(@RequestBody StatisticsGroup entity) {
        // Validate required fields
        if (entity.getName() == null || entity.getName().trim().isEmpty()) {
            throw new BusinessException(BusinessCode.BadRequest.getCode(), "Group name cannot be null or empty");
        }
        if (entity.getTitle() == null || entity.getTitle().trim().isEmpty()) {
            throw new BusinessException(BusinessCode.BadRequest.getCode(), "Group title cannot be null or empty");
        }
        return SuccessTip.create(statisticsGroupService.createGroup(entity));
    }

    @ApiOperation(value = "修改组", response = StatisticsGroup.class)
    @PutMapping("/{id}")
    public Tip updateConfigGroupAllColumns(@PathVariable Long id, @RequestBody StatisticsGroup entity) {
        // Validate required fields
        if (entity.getName() == null || entity.getName().trim().isEmpty()) {
            throw new BusinessException(BusinessCode.BadRequest.getCode(), "Group name cannot be null or empty");
        }
        if (entity.getTitle() == null || entity.getTitle().trim().isEmpty()) {
            throw new BusinessException(BusinessCode.BadRequest.getCode(), "Group title cannot be null or empty");
        }
        entity.setId(id);
        return SuccessTip.create(statisticsGroupService.updateGroup(entity, true));
    }

    @ApiOperation(value = "修改组（选择具体某项修改）", response = StatisticsGroup.class)
    @PatchMapping("/{id}")
    public Tip updateConfigGroup(@PathVariable Long id, @RequestBody StatisticsGroup entity) {
        // Retrieve existing group to preserve required fields
        StatisticsGroup existing = statisticsGroupService.retrieveGroup(id);
        if (existing == null) {
            throw new BusinessException(BusinessCode.CRUD_QUERY_FAILURE, "StatisticsGroup not found with id: " + id);
        }

        // Preserve name and title if not provided
        if (entity.getName() == null || entity.getName().trim().isEmpty()) {
            entity.setName(existing.getName());
        }
        if (entity.getTitle() == null || entity.getTitle().trim().isEmpty()) {
            entity.setTitle(existing.getTitle());
        }

        entity.setId(id);
        return SuccessTip.create(statisticsGroupService.updateGroup(entity, false));
    }
}
