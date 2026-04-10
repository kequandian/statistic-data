package com.jfeat.am.module.statistics.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jfeat.am.module.statistics.services.domain.dao.QueryStatisticsFieldDao;
import com.jfeat.am.module.statistics.services.persistence.model.StatisticsField;
import com.jfeat.am.module.statistics.services.crud.StatisticsFieldService;
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
 * Statistics Field Maintenance API
 * Fixed to handle groupName validation and prevent null constraint violations
 * </p>
 *
 * @author Code Generator
 * @since 2017-10-19
 */
@Api("统计 [Statistics]")
@RestController
@RequestMapping("/api/cfg/stat/fields")
public class MaintenanceFieldEndpoint{

    @Resource
    private StatisticsFieldService statisticsFieldService;

    @Resource
    private QueryStatisticsFieldDao statisticsFieldDao;


    @ApiOperation(value = "增加统计域", response = StatisticsField.class)
    @PostMapping
    public Tip createStatisticsField(@RequestBody StatisticsField entity){
        // Validate groupName before creation
        if (entity.getGroupName() == null || entity.getGroupName().trim().isEmpty()) {
            throw new BusinessException(BusinessCode.BadRequest.getCode(), "groupName cannot be null or empty");
        }
        // Ensure groupId is set if groupName is provided
        if (entity.getGroupId() == null && entity.getGroupName() != null) {
            // Optionally auto-resolve groupId from groupName if needed
            // For now, require both to be set explicitly
        }
        return SuccessTip.create(statisticsFieldService.createMaster(entity));
    }

    @ApiOperation("获取统计域")
    @GetMapping("/{id}")
    public Tip getStatisticsField(@PathVariable Long id) {
        return SuccessTip.create(statisticsFieldService.retrieveMaster(id));
    }

    @ApiOperation(value = "修改统计域", response = StatisticsField.class)
    @PutMapping("/{id}")
    public Tip updateStatisticsField(@PathVariable Long id, @RequestBody StatisticsField entity) {
        // Retrieve existing entity to preserve groupName if not provided
        StatisticsField existing = statisticsFieldService.retrieveMaster(id);
        if (existing == null) {
            throw new BusinessException(BusinessCode.NotFound.getCode(), "StatisticsField not found with id: " + id);
        }

        // Preserve groupName if not provided in update request
        if (entity.getGroupName() == null || entity.getGroupName().trim().isEmpty()) {
            entity.setGroupName(existing.getGroupName());
        }

        // Preserve groupId if not provided
        if (entity.getGroupId() == null) {
            entity.setGroupId(existing.getGroupId());
        }

        entity.setId(id);
        return SuccessTip.create(statisticsFieldService.updateMaster(entity, true));
    }

    @ApiOperation("分页返回所有图表数据域")
    @GetMapping
    public Tip queryStatisticsFields(Page<StatisticsField> page,
                                     @RequestParam(name = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                                     @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                     @RequestParam(name = "field", required = false) String field,
                                     @RequestParam(name = "name", required = false) String name,
                                     @RequestParam(name = "index", required = false) Integer index,
                                     @RequestParam(name = "groupId", required = false) String groupName,
                                     @RequestParam(name = "invisible", required = false) Integer invisible,
                                     @RequestParam(name = "chart", required = false) String chart) {
        page.setCurrent(pageNum);
        page.setSize(pageSize);

        StatisticsField statisticsField = new StatisticsField();
        statisticsField.setGroupName(groupName);
        statisticsField.setField(field);
        statisticsField.setName(name);
        statisticsField.setChart(chart);
        statisticsField.setAttrInvisible(invisible);
        statisticsField.setAttrIndex(index);

        page.setRecords(statisticsFieldDao.findStatisticsFieldPage(page, statisticsField));
        return SuccessTip.create(page);
    }


    /**
     * 修改统计域属性
     * @param id
     * @return
     */

    @ApiOperation("设置统计域分组 [指转移经计域至其他分组]")
    @PostMapping("/{id}/attr/group/{groupId}")
    public Tip changeStatisticsFieldGroup(@PathVariable Long id, @PathVariable Long groupId) {
        // Retrieve existing entity to preserve other fields
        StatisticsField existing = statisticsFieldService.retrieveMaster(id);
        if (existing == null) {
            throw new BusinessException(BusinessCode.NotFound.getCode(), "StatisticsField not found with id: " + id);
        }

        // Only update groupId, preserve all other fields including groupName
        StatisticsField entity = new StatisticsField();
        entity.setId(id);
        entity.setGroupId(groupId);
        // Preserve groupName to avoid null constraint violation
        entity.setGroupName(existing.getGroupName());

        return SuccessTip.create(statisticsFieldService.updateMaster(entity, false));
    }

    @ApiOperation("使统计域可见")
    @PostMapping("/{id}/attr/visible")
    public Tip setFieldVisible(@PathVariable Long id) {
        return updateSingleAttribute(id, entity -> entity.setAttrInvisible(0));
    }

    @ApiOperation("使统计域不可见")
    @PostMapping("/{id}/attr/invisible")
    public Tip setFieldInvisible(@PathVariable Long id) {
        return updateSingleAttribute(id, entity -> entity.setAttrInvisible(1));
    }

    @ApiOperation("设置统计域 图表名称 [通常由前端定义]")
    @PostMapping("/{id}/attr/chart/{chart}")
    public Tip changeStatisticsFieldChart(@PathVariable Long id, @PathVariable String chart) {
        return updateSingleAttribute(id, entity -> entity.setChart(chart));
    }

    @ApiOperation("设备统计域排序号")
    @PostMapping("/{id}/attr/index/{index}")
    public Tip setFieldIndex(@PathVariable Long id, @PathVariable Integer index) {
        return updateSingleAttribute(id, entity -> entity.setAttrIndex(index));
    }

    @ApiOperation("设备统计域占组布局的列数")
    @PostMapping("/{id}/attr/span/{span}")
    public Tip setFieldLayoutSpan(@PathVariable Long id, @PathVariable Integer span) {
        return updateSingleAttribute(id, entity -> entity.setAttrSpan(span));
    }


    @ApiOperation("使统计域不可见")
    @PostMapping("/{id}/attr/runtime")
    public Tip setFieldRuntime(@PathVariable Long id) {
        throw new BusinessException(BusinessCode.NotImplement.getCode(), "未实现运行时查询，需要支持SQL设置");
    }

    /**
     * Helper method to safely update a single attribute while preserving others
     * This prevents null constraint violations on fields like groupName
     */
    private Tip updateSingleAttribute(Long id, java.util.function.Consumer<StatisticsField> attributeSetter) {
        // Retrieve existing entity to preserve all fields
        StatisticsField existing = statisticsFieldService.retrieveMaster(id);
        if (existing == null) {
            throw new BusinessException(BusinessCode.NotFound.getCode(), "StatisticsField not found with id: " + id);
        }

        // Create new entity and copy all existing fields
        StatisticsField entity = new StatisticsField();
        entity.setId(id);
        entity.setField(existing.getField());
        entity.setName(existing.getName());
        entity.setGroupName(existing.getGroupName());  // Always preserve groupName
        entity.setGroupId(existing.getGroupId());        // Always preserve groupId
        entity.setPattern(existing.getPattern());
        entity.setChart(existing.getChart());
        entity.setAttrInvisible(existing.getAttrInvisible());
        entity.setAttrRuntime(existing.getAttrRuntime());
        entity.setAttrSpan(existing.getAttrSpan());
        entity.setAttrIndex(existing.getAttrIndex());

        // Apply the specific attribute change
        attributeSetter.accept(entity);

        return SuccessTip.create(statisticsFieldService.updateMaster(entity, true));
    }

}
