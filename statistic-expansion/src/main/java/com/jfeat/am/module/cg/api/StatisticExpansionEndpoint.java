package com.jfeat.am.module.cg.api;


import com.jfeat.am.module.cg.services.domain.service.StatisticExpansionService;
import com.jfeat.am.module.menu.services.gen.persistence.dao.MenuMapper;
import com.jfeat.am.module.menu.services.gen.persistence.model.Menu;
import com.jfeat.am.module.statistics.services.crud.StatisticsMetaService;
import com.jfeat.am.module.statistics.services.domain.model.StatisticsMetaRecord;
import com.jfeat.am.module.statistics.services.gen.persistence.model.StatisticsMeta;
import com.jfeat.am.module.statistics.util.MetaUtil;
import com.jfeat.crud.base.annotation.BusinessLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.DuplicateKeyException;
import com.jfeat.crud.base.tips.SuccessTip;
import com.jfeat.crud.base.tips.Tip;
import com.jfeat.crud.base.exception.BusinessCode;
import com.jfeat.crud.base.exception.BusinessException;
import com.jfeat.am.module.cg.api.permission.*;
import com.jfeat.am.common.annotation.Permission;
import javax.annotation.Resource;

/**
 * <p>
 * api
 * </p>
 *
 * @author Code generator
 * @since 2021-03-05
 */
@RestController

@Api("StatisticExpansion")
@RequestMapping("/api/adm/stat/expansion")
public class StatisticExpansionEndpoint {

    @Resource
    StatisticExpansionService statisticExpansionService;

    @Resource
    StatisticsMetaService statisticsMetaService;

    @Resource
    MenuMapper menuMapper;

    @BusinessLog(name = "MasterResource", value = "create MasterResource")
    @Permission(StatisticExpansionPermission.STATISTIC_EXPANSION_NEW)
    @PostMapping
    @ApiOperation(value = "新建 MasterResource")
    public Tip createMasterResource(@RequestBody StatisticsMetaRecord entity) {
        Integer affected = 0;
        //类型进行映射
        entity.setType(MetaUtil.replaceType(entity.getType()));
        try {

            if (entity.getGroupMenuId() != null) {
                affected = statisticExpansionService.createStatisticAndMenu(entity);
            } else {
                affected = statisticsMetaService.createMaster(entity);
            }

        } catch (DuplicateKeyException e) {
            throw new BusinessException(BusinessCode.DuplicateKey);
        }

        return SuccessTip.create(affected);
    }


    @BusinessLog(name = "StatisticsMeta", value = "update StatisticsMeta")
    @PutMapping("/{id}")
    @ApiOperation(value = "修改 StatisticsMeta", response = StatisticsMetaRecord.class)
    public Tip updateStatisticsMeta(@PathVariable Long id, @RequestBody StatisticsMetaRecord entity) {
        //类型进行映射
        entity.setType(MetaUtil.replaceType(entity.getType()));
        entity.setId(id);
        if (entity.getGroupMenuId() != null) {
            //菜单组id不为空 更新对应菜单
            if (entity.getMenuId() == null) {
                throw new BusinessException(4001, "此报表没菜单");
            }
            Menu menu = menuMapper.selectById(entity.getMenuId());
            Menu pMenu = menuMapper.selectById(entity.getGroupMenuId());
            if (menu == null) {
                throw new BusinessException(4001, "报表对应的菜单不存在");
            }
            menu.setPid(entity.getGroupMenuId());
            menu.setComponent(pMenu.getComponent() + "/table?id=" + id);
            menuMapper.updateById(menu);
        }
        return SuccessTip.create(statisticsMetaService.updateMaster(entity));
    }


    @BusinessLog(name = "StatisticsMeta", value = "delete StatisticsMeta")
    @DeleteMapping("/{id}")
    @ApiOperation("删除 StatisticsMeta")
    public Tip deleteStatisticsMeta(@PathVariable Long id) {
        StatisticsMeta statisticsMeta = statisticsMetaService.retrieveMaster(id);
        if(statisticsMeta!=null && statisticsMeta.getMenuId()!=null){
            menuMapper.deleteById(statisticsMeta.getMenuId());
        }
        return SuccessTip.create(statisticsMetaService.deleteMaster(id));
    }


}
