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

import javax.annotation.Resource;

/**
 * <p>
 * api
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
        StatisticsField entity = new StatisticsField();
        entity.setId(id);
        entity.setGroupId(groupId);

        return SuccessTip.create(statisticsFieldService.updateMaster(entity, false));
    }

    @ApiOperation("使统计域可见")
    @PostMapping("/{id}/attr/visible")
    public Tip setFieldVisible(@PathVariable Long id) {
        StatisticsField entity = new StatisticsField();
        entity.setId(id);
        entity.setAttrInvisible(0);

        return SuccessTip.create(statisticsFieldService.updateMaster(entity, false));
    }

    @ApiOperation("使统计域不可见")
    @PostMapping("/{id}/attr/invisible")
    public Tip setFieldInvisible(@PathVariable Long id) {
        StatisticsField entity = new StatisticsField();
        entity.setId(id);
        entity.setAttrInvisible(1);

        return SuccessTip.create(statisticsFieldService.updateMaster(entity, false));
    }

    @ApiOperation("设置统计域 图表名称 [通常由前端定义]")
    @PostMapping("/{id}/attr/chart/{chart}")
    public Tip changeStatisticsFieldChart(@PathVariable Long id, @PathVariable String chart) {
        StatisticsField entity = new StatisticsField();
        entity.setId(id);
        entity.setChart(chart);

        return SuccessTip.create(statisticsFieldService.updateMaster(entity, false));
    }

    @ApiOperation("设备统计域排序号")
    @PostMapping("/{id}/attr/index/{index}")
    public Tip setFieldIndex(@PathVariable Long id, @PathVariable Integer index) {
        StatisticsField entity = new StatisticsField();
        entity.setId(id);
        entity.setAttrIndex(index);

        return SuccessTip.create(statisticsFieldService.updateMaster(entity, false));
    }

    @ApiOperation("设备统计域占组布局的列数")
    @PostMapping("/{id}/attr/span/{span}")
    public Tip setFieldLayoutSpan(@PathVariable Long id, @PathVariable Integer span) {
        StatisticsField entity = new StatisticsField();
        entity.setId(id);
        entity.setAttrSpan(span);

        return SuccessTip.create(statisticsFieldService.updateMaster(entity, false));
    }


    @ApiOperation("使统计域不可见")
    @PostMapping("/{id}/attr/runtime")
    public Tip setFieldRuntime(@PathVariable Long id) {
        throw new BusinessException(BusinessCode.NotImplement.getCode(), "未实现运行时查询，需要支持SQL设置");
    }

}
