package com.jfeat.am.module.statistics.api;


import com.jfeat.crud.base.annotation.BusinessLog;
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
import com.jfeat.am.module.statistics.services.domain.dao.QueryStatisticsUsedHistoryDao;
import com.jfeat.crud.base.tips.SuccessTip;
import com.jfeat.crud.base.tips.Tip;
import com.jfeat.crud.base.exception.BusinessCode;
import com.jfeat.crud.base.exception.BusinessException;
import com.jfeat.am.module.statistics.api.permission.*;
import com.jfeat.am.common.annotation.Permission;

import com.jfeat.am.module.statistics.services.domain.service.*;
import com.jfeat.am.module.statistics.services.domain.model.StatisticsUsedHistoryRecord;
import com.jfeat.am.module.statistics.services.gen.persistence.model.StatisticsUsedHistory;

import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * api
 * </p>
 *
 * @author Code generator
 * @since 2022-08-31
 */
@RestController

@Api("StatisticsUsedHistory")
@RequestMapping("/api/adm/statisticsUsedHistory/statisticsUsedHistoryies")
public class StatisticsUsedHistoryEndpoint {

    @Resource
    StatisticsUsedHistoryService statisticsUsedHistoryService;


    @Resource
    QueryStatisticsUsedHistoryDao queryStatisticsUsedHistoryDao;

    @BusinessLog(name = "StatisticsUsedHistory", value = "create StatisticsUsedHistory")
    @Permission(StatisticsUsedHistoryPermission.STATISTICSUSEDHISTORY_NEW)
    @PostMapping
    @ApiOperation(value = "新建 StatisticsUsedHistory", response = StatisticsUsedHistory.class)
    public Tip createStatisticsUsedHistory(@RequestBody StatisticsUsedHistory entity) {

        Integer affected = 0;
        try {
            affected = statisticsUsedHistoryService.createMaster(entity);

        } catch (DuplicateKeyException e) {
            throw new BusinessException(BusinessCode.DuplicateKey);
        }

        return SuccessTip.create(affected);
    }

    @Permission(StatisticsUsedHistoryPermission.STATISTICSUSEDHISTORY_VIEW)
    @GetMapping("/{id}")
    @ApiOperation(value = "查看 StatisticsUsedHistory", response = StatisticsUsedHistory.class)
    public Tip getStatisticsUsedHistory(@PathVariable Long id) {
        return SuccessTip.create(statisticsUsedHistoryService.queryMasterModel(queryStatisticsUsedHistoryDao, id));
    }

    @BusinessLog(name = "StatisticsUsedHistory", value = "update StatisticsUsedHistory")
    @Permission(StatisticsUsedHistoryPermission.STATISTICSUSEDHISTORY_EDIT)
    @PutMapping("/{id}")
    @ApiOperation(value = "修改 StatisticsUsedHistory", response = StatisticsUsedHistory.class)
    public Tip updateStatisticsUsedHistory(@PathVariable Long id, @RequestBody StatisticsUsedHistory entity) {
        entity.setId(id);
        return SuccessTip.create(statisticsUsedHistoryService.updateMaster(entity));
    }

    @BusinessLog(name = "StatisticsUsedHistory", value = "delete StatisticsUsedHistory")
    @Permission(StatisticsUsedHistoryPermission.STATISTICSUSEDHISTORY_DELETE)
    @DeleteMapping("/{id}")
    @ApiOperation("删除 StatisticsUsedHistory")
    public Tip deleteStatisticsUsedHistory(@PathVariable Long id) {
        return SuccessTip.create(statisticsUsedHistoryService.deleteMaster(id));
    }

    @Permission(StatisticsUsedHistoryPermission.STATISTICSUSEDHISTORY_VIEW)
    @ApiOperation(value = "StatisticsUsedHistory 列表信息", response = StatisticsUsedHistoryRecord.class)
    @GetMapping
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNum", dataType = "Integer"),
            @ApiImplicitParam(name = "pageSize", dataType = "Integer"),
            @ApiImplicitParam(name = "search", dataType = "String"),
            @ApiImplicitParam(name = "id", dataType = "Long"),
            @ApiImplicitParam(name = "createTime", dataType = "Date"),
            @ApiImplicitParam(name = "updateTime", dataType = "Date"),
            @ApiImplicitParam(name = "userId", dataType = "Long"),
            @ApiImplicitParam(name = "metaId", dataType = "Long"),
            @ApiImplicitParam(name = "metaTitle", dataType = "String"),
            @ApiImplicitParam(name = "comeForm", dataType = "String"),
            @ApiImplicitParam(name = "comeFormType", dataType = "String"),
            @ApiImplicitParam(name = "orderBy", dataType = "String"),
            @ApiImplicitParam(name = "sort", dataType = "String")
    })
    public Tip queryStatisticsUsedHistoryies(Page<StatisticsUsedHistoryRecord> page,
                                             @RequestParam(name = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                                             @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                             @RequestParam(name = "search", required = false) String search,
                                             @RequestParam(name = "id", required = false) Long id,

                                             @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                             @RequestParam(name = "createTime", required = false) Date createTime,

                                             @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                             @RequestParam(name = "updateTime", required = false) Date updateTime,

                                             @RequestParam(name = "userId", required = false) Long userId,

                                             @RequestParam(name = "metaId", required = false) Long metaId,

                                             @RequestParam(name = "metaTitle", required = false) String metaTitle,

                                             @RequestParam(name = "comeForm", required = false) String comeForm,

                                             @RequestParam(name = "comeFormType", required = false) String comeFormType,
                                             @RequestParam(name = "orderBy", required = false) String orderBy,
                                             @RequestParam(name = "sort", required = false) String sort) {

        if (orderBy != null && orderBy.length() > 0) {
            if (sort != null && sort.length() > 0) {
                String pattern = "(ASC|DESC|asc|desc)";
                if (!sort.matches(pattern)) {
                    throw new BusinessException(BusinessCode.BadRequest.getCode(), "sort must be ASC or DESC");//此处异常类型根据实际情况而定
                }
            } else {
                sort = "ASC";
            }
            orderBy = "`" + orderBy + "`" + " " + sort;
        }
        page.setCurrent(pageNum);
        page.setSize(pageSize);

        StatisticsUsedHistoryRecord record = new StatisticsUsedHistoryRecord();
        record.setId(id);
        record.setCreateTime(createTime);
        record.setUpdateTime(updateTime);
        record.setUserId(userId);
        record.setMetaId(metaId);
        record.setMetaTitle(metaTitle);
        record.setComeForm(comeForm);
        record.setComeFormType(comeFormType);


        List<StatisticsUsedHistoryRecord> statisticsUsedHistoryPage = queryStatisticsUsedHistoryDao.findStatisticsUsedHistoryPage(page, record, search, orderBy, null, null);

        page.setRecords(statisticsUsedHistoryPage);

        return SuccessTip.create(page);
    }
}
