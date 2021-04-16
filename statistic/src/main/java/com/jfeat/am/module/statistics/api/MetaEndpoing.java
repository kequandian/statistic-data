package com.jfeat.am.module.statistics.api;

//import com.jfeat.am.module.log.annotation.BusinessLog;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jfeat.am.common.annotation.UrlPermission;
import com.jfeat.am.module.log.annotation.BusinessLog;
import com.jfeat.am.module.statistics.api.model.MetaColumns;
import com.jfeat.am.module.statistics.api.model.MetaGroupTemplate;
import com.jfeat.am.module.statistics.api.model.MetaTag;
import com.jfeat.am.module.statistics.api.template.TemplateSetting;
import com.jfeat.am.module.statistics.services.crud.ExtendedStatistics;
import com.jfeat.am.module.statistics.services.crud.StatisticsMetaService;
import com.jfeat.am.module.statistics.services.domain.dao.QueryStatisticsMetaDao;
import com.jfeat.am.module.statistics.services.domain.model.StatisticsMetaRecord;
import com.jfeat.am.module.statistics.services.domain.service.StatisticsMetaGroupService;
import com.jfeat.am.module.statistics.services.gen.persistence.dao.StatisticsMetaMapper;
import com.jfeat.am.module.statistics.services.gen.persistence.model.StatisticsMeta;
import com.jfeat.am.module.statistics.util.MetaUtil;
import com.jfeat.crud.base.exception.BusinessCode;
import com.jfeat.crud.base.exception.BusinessException;
import com.jfeat.crud.base.tips.SuccessTip;
import com.jfeat.crud.base.tips.Tip;
import com.jfeat.crud.plus.CRUD;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

@Api("统计 [Statistics] meta")
@RestController
@RequestMapping("/api/adm/stat/meta")
public class MetaEndpoing {

    @Resource
    StatisticsMetaService statisticsMetaService;
    @Resource
    QueryStatisticsMetaDao queryStatisticsMetaDao;
    @Resource
    ExtendedStatistics extendedStatistics;
    @Resource
    StatisticsMetaGroupService statisticsMetaGroupService;
    @Resource
    StatisticsMetaMapper statisticsMetaMapper;

    @ApiOperation("根据字段获取报表")
    @GetMapping("/{field}")
    public Tip getConfigList(@PathVariable String field,
                             @RequestParam(name = "pageNum", required = false, defaultValue = "1") Long current,
                             @RequestParam(name = "pageSize", required = false, defaultValue = "10") Long size
    ) {
        MetaTag metaTag = new MetaTag();
        metaTag.setCurrent(current);
        metaTag.setSize(size);
        JSONObject data = extendedStatistics.getByPattern(field, metaTag);
        return SuccessTip.create(data);
    }


    @UrlPermission
    @ApiOperation("获取分组报表，获得已有配置")
    @GetMapping("/template/{groupName}")
    public Tip getConfigGroupList(@PathVariable String groupName,
                                  @RequestParam(name = "pageNum", required = false, defaultValue = "1") Long current,
                                  @RequestParam(name = "pageSize", required = false, defaultValue = "10") Long size) {
        MetaTag metaTag = new MetaTag();
        metaTag.setCurrent(current);
        metaTag.setSize(size);
        JSONObject template = statisticsMetaGroupService.getTemplateByName(groupName, metaTag);
        return SuccessTip.create(template);
    }


    @BusinessLog(name = "StatisticsMeta", value = "create StatisticsMeta")
    @PostMapping
    @ApiOperation(value = "新建 StatisticsMeta", response = StatisticsMeta.class)
    public Tip createStatisticsMeta(@RequestBody StatisticsMeta entity) {

        Integer affected = 0;
        try {
            //类型进行映射
            entity.setType(MetaUtil.replaceType(entity.getType()));
            affected = statisticsMetaService.createMaster(entity);

        } catch (DuplicateKeyException e) {
            throw new BusinessException(BusinessCode.DuplicateKey);
        }

        return SuccessTip.create(affected);
    }

    @BusinessLog(name = "StatisticsMeta", value = "查看 StatisticsMeta")
    @GetMapping("/getOne/{id}")
    @ApiOperation(value = "查看 StatisticsMeta", response = StatisticsMeta.class)
    public Tip getStatisticsMeta(@PathVariable Long id) {
        StatisticsMeta statisticsMeta = statisticsMetaService.retrieveMaster(id);
//        //类型进行映射
        statisticsMeta.setType(MetaUtil.transientType(statisticsMeta.getType()));
        return SuccessTip.create(statisticsMeta);
    }

    @BusinessLog(name = "StatisticsMeta", value = "update StatisticsMeta")
    @PutMapping("/{id}")
    @ApiOperation(value = "修改 StatisticsMeta", response = StatisticsMeta.class)
    public Tip updateStatisticsMeta(@PathVariable Long id, @RequestBody StatisticsMeta entity) {
        entity.setId(id);
        return SuccessTip.create(statisticsMetaService.updateMaster(entity));
    }

    @BusinessLog(name = "StatisticsMeta", value = "delete StatisticsMeta")
    @DeleteMapping("/{id}")
    @ApiOperation("删除 StatisticsMeta")
    public Tip deleteStatisticsMeta(@PathVariable Long id) {
        return SuccessTip.create(statisticsMetaService.deleteMaster(id));
    }

    @BusinessLog(name = "StatisticsMeta", value = "查询列表 StatisticsMeta")
    @ApiOperation(value = "StatisticsMeta 列表信息", response = StatisticsMetaRecord.class)
    @GetMapping
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNum", dataType = "Integer"),
            @ApiImplicitParam(name = "pageSize", dataType = "Integer"),
            @ApiImplicitParam(name = "search", dataType = "String"),
            @ApiImplicitParam(name = "id", dataType = "Long"),
            @ApiImplicitParam(name = "field", dataType = "String"),
            @ApiImplicitParam(name = "querySql", dataType = "String"),
            @ApiImplicitParam(name = "percent", dataType = "Integer"),
            @ApiImplicitParam(name = "icon", dataType = "String"),
            @ApiImplicitParam(name = "title", dataType = "String"),
            @ApiImplicitParam(name = "type", dataType = "String"),
            @ApiImplicitParam(name = "search", dataType = "String"),
            @ApiImplicitParam(name = "permission", dataType = "String"),
            @ApiImplicitParam(name = "orderBy", dataType = "String"),
            @ApiImplicitParam(name = "sort", dataType = "String")
    })
    public Tip queryStatisticsMetas(Page<StatisticsMetaRecord> page,
                                    @RequestParam(name = "pageNum", required = false, defaultValue = "1") Integer pageNum,
                                    @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize,
                                    @RequestParam(name = "search", required = false) String search,
                                    @RequestParam(name = "id", required = false) Long id,
                                    @RequestParam(name = "field", required = false) String field,
                                    @RequestParam(name = "querySql", required = false) String querySql,
                                    @RequestParam(name = "percent", required = false) Integer percent,
                                    @RequestParam(name = "icon", required = false) String icon,
                                    @RequestParam(name = "title", required = false) String title,
                                    @RequestParam(name = "type", required = false) String type,
                                    @RequestParam(name = "permission", required = false) String permission,
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

        StatisticsMetaRecord record = new StatisticsMetaRecord();
        record.setId(id);
        record.setField(field);
        record.setQuerySql(querySql);
        record.setPercent(percent);
        record.setIcon(icon);
        record.setTitle(title);
        record.setType(type);
        record.setPermission(permission);
        List<StatisticsMetaRecord> result = queryStatisticsMetaDao.findStatisticsMetaPage(page, record, search, orderBy, null, null);
        for (StatisticsMetaRecord resu : result) {
            //类型进行映射
            resu.setChinceseType(MetaUtil.transientType(resu.getType()));
        }
        page.setRecords(result);

        return SuccessTip.create(page);
    }

    @BusinessLog(name = "StatisticsMeta", value = "get StatisticsMeta")
    @GetMapping("/jsonsetting/{id}")
    @ApiOperation("获取 jsonsetting")
    public Tip getJsonSetting(@PathVariable Long id) {
        StatisticsMeta statisticsMeta = statisticsMetaService.retrieveMaster(id);
        String field = statisticsMeta.getField();
        String title = statisticsMeta.getTitle();
        return SuccessTip.create(statisticsMetaService.getOutputSetting(field, title));
    }

    @BusinessLog(name = "StatisticsMeta", value = "get StatisticsMeta")
    @GetMapping("/jsonsetting")
    @ApiOperation("无id时获取 空的jsonsetting")
    public Tip getJsonSetting() {
        return SuccessTip.create(null);
    }

    @BusinessLog(name = "StatisticsMeta", value = "查询列表 StatisticsMeta")
    @ApiOperation(value = "StatisticsMeta 列表信息 无分页", response = StatisticsMetaRecord.class)
    @GetMapping("/list")
    public Tip queryStatisticsMetas() {
        List<StatisticsMeta> statisticsMetaList = statisticsMetaMapper.selectList(new QueryWrapper<StatisticsMeta>());
        return SuccessTip.create(statisticsMetaList);
    }
}
