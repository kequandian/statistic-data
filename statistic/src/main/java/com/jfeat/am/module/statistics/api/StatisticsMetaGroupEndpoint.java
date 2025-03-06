package com.jfeat.am.module.statistics.api;


import com.jfeat.crud.plus.META;
import com.jfeat.am.core.jwt.JWTKit;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.dao.DuplicateKeyException;
import com.jfeat.am.module.statistics.services.domain.dao.QueryStatisticsMetaGroupDao;
import com.jfeat.crud.base.tips.SuccessTip;
import com.jfeat.crud.base.request.Ids;
import com.jfeat.crud.base.tips.Tip;
import com.jfeat.crud.base.annotation.BusinessLog;
import com.jfeat.crud.base.exception.BusinessCode;
import com.jfeat.crud.base.exception.BusinessException;
import com.jfeat.crud.plus.CRUDObject;
import com.jfeat.am.module.statistics.api.permission.*;
import com.jfeat.am.common.annotation.Permission;

import java.math.BigDecimal;

import com.jfeat.am.module.statistics.services.domain.service.*;
import com.jfeat.am.module.statistics.services.domain.model.StatisticsMetaGroupRecord;
import com.jfeat.am.module.statistics.services.gen.persistence.model.StatisticsMetaGroup;

import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;

/**
 * <p>
 * api
 * </p>
 *
 * @author Code generator
 * @since 2020-10-27
 */
@RestController

@Api("StatisticsMetaGroup")
@RequestMapping("/api/adm/statisticsMetaGroup/statisticsMetaGroups")
public class StatisticsMetaGroupEndpoint {

    @Resource
    StatisticsMetaGroupService statisticsMetaGroupService;


    @Resource
    QueryStatisticsMetaGroupDao queryStatisticsMetaGroupDao;

    @BusinessLog(name = "StatisticsMetaGroup", value = "create StatisticsMetaGroup")
    @Permission(StatisticsMetaGroupPermission.STATISTICSMETAGROUP_NEW)
    @PostMapping
    @ApiOperation(value = "新建 StatisticsMetaGroup", response = StatisticsMetaGroup.class)
    public Tip createStatisticsMetaGroup(@RequestBody StatisticsMetaGroup entity) {

        Integer affected = 0;
        try {
            affected = statisticsMetaGroupService.createMaster(entity);

        } catch (DuplicateKeyException e) {
            throw new BusinessException(BusinessCode.DuplicateKey);
        }

        return SuccessTip.create(affected);
    }

    @Permission(StatisticsMetaGroupPermission.STATISTICSMETAGROUP_VIEW)
    @GetMapping("/{id}")
    @ApiOperation(value = "查看 StatisticsMetaGroup", response = StatisticsMetaGroup.class)
    public Tip getStatisticsMetaGroup(@PathVariable Long id) {
        return SuccessTip.create(statisticsMetaGroupService.queryMasterModel(queryStatisticsMetaGroupDao, id));
    }

    @BusinessLog(name = "StatisticsMetaGroup", value = "update StatisticsMetaGroup")
    @Permission(StatisticsMetaGroupPermission.STATISTICSMETAGROUP_EDIT)
    @PutMapping("/{id}")
    @ApiOperation(value = "修改 StatisticsMetaGroup", response = StatisticsMetaGroup.class)
    public Tip updateStatisticsMetaGroup(@PathVariable Long id, @RequestBody StatisticsMetaGroup entity) {
        entity.setId(id);
        return SuccessTip.create(statisticsMetaGroupService.updateMaster(entity));
    }

    @BusinessLog(name = "StatisticsMetaGroup", value = "delete StatisticsMetaGroup")
    @Permission(StatisticsMetaGroupPermission.STATISTICSMETAGROUP_DELETE)
    @DeleteMapping("/{id}")
    @ApiOperation("删除 StatisticsMetaGroup")
    public Tip deleteStatisticsMetaGroup(@PathVariable Long id) {
        return SuccessTip.create(statisticsMetaGroupService.deleteMaster(id));
    }

    @Permission(StatisticsMetaGroupPermission.STATISTICSMETAGROUP_VIEW)
    @ApiOperation(value = "StatisticsMetaGroup 列表信息", response = StatisticsMetaGroupRecord.class)
    @GetMapping
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNum", dataType = "Integer"),
            @ApiImplicitParam(name = "pageSize", dataType = "Integer"),
            @ApiImplicitParam(name = "search", dataType = "String"),
            @ApiImplicitParam(name = "id", dataType = "Long"),
            @ApiImplicitParam(name = "name", dataType = "String"),
            @ApiImplicitParam(name = "pid", dataType = "Long"),
            @ApiImplicitParam(name = "pattern", dataType = "String"),
            @ApiImplicitParam(name = "span", dataType = "Integer"),
            @ApiImplicitParam(name = "layout", dataType = "String"),
            @ApiImplicitParam(name = "title", dataType = "String"),
            @ApiImplicitParam(name = "orderBy", dataType = "String"),
            @ApiImplicitParam(name = "sort", dataType = "String")
    })
    public Tip queryStatisticsMetaGroups(Page<StatisticsMetaGroupRecord> page,
                                         @RequestParam(name = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                                         @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                         @RequestParam(name = "search", required = false) String search,
                                         @RequestParam(name = "id", required = false) Long id,
                                         @RequestParam(name = "name", required = false) String name,
                                         @RequestParam(name = "pid", required = false) Long pid,
                                         @RequestParam(name = "pattern", required = false) String pattern,
                                         @RequestParam(name = "span", required = false) Integer span,
                                         @RequestParam(name = "layout", required = false) String layout,
                                         @RequestParam(name = "title", required = false) String title,
                                         @RequestParam(name = "orderBy", required = false) String orderBy,
                                         @RequestParam(name = "sort", required = false) String sort) {
        page.setCurrent(pageNum);
        page.setSize(pageSize);

        StatisticsMetaGroupRecord record = new StatisticsMetaGroupRecord();
        record.setId(id);
        record.setName(name);
        record.setPid(pid);
        record.setPattern(pattern);
        record.setSpan(span);
        record.setLayout(layout);
        record.setTitle(title);
        page.setRecords(queryStatisticsMetaGroupDao.findStatisticsMetaGroupPage(page, record, search, orderBy, null, null));

        return SuccessTip.create(page);
    }
}
