package com.jfeat.am.module.cg.services.domain.service.impl;

import com.jfeat.am.core.jwt.JWTKit;
import com.jfeat.am.module.cg.services.domain.service.StatisticExpansionService;
import com.jfeat.am.module.menu.services.domain.model.MenuType;
import com.jfeat.am.module.menu.services.domain.service.MenuService;
import com.jfeat.am.module.menu.services.gen.persistence.model.Menu;
import com.jfeat.am.module.menu.util.MenuUtil;
import com.jfeat.am.module.statistics.services.crud.StatisticsMetaService;
import com.jfeat.am.module.statistics.services.domain.model.StatisticsMetaRecord;
import com.jfeat.crud.base.exception.BusinessCode;
import com.jfeat.crud.base.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.io.File;

import static com.jfeat.am.module.statistics.api.perm.StatisticsMetaPermission.DEFAULT_REPORT_PERM_ID;
import static com.jfeat.am.module.statistics.api.perm.StatisticsMetaPermission.DEFAULT_REPORT_VIEW;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author admin
 * @since 2017-10-16
 */

@Service("StatisticExpansion")
public class StatisticExpansionServiceImpl implements StatisticExpansionService {


    @Resource
    StatisticsMetaService statisticsMetaService;
    @Resource
    MenuService menuService;

    @Override
    @Transactional
    public Integer createStatisticAndMenu(StatisticsMetaRecord meta){
        Integer res = 0;
        /***      生成前端代码   取消       **/
        //String webIndex = genWebCode(meta);
        /***      获取父类菜单路径          **/
        meta.setMenuId(null);
        Menu pMenu = menuService.retrieveMaster(meta.getGroupMenuId());
        //   /父菜单/table?table=field

        /***      创建菜单          **/
        Menu menu = MenuUtil.getInitMenu();
        menu.setPid(meta.getGroupMenuId());
        menu.setMenuName(meta.getTitle());
        menu.setOrgId(JWTKit.getTenantOrgId());
        menu.setMenuType(MenuType.MENU);
        menu.setPermId(DEFAULT_REPORT_PERM_ID);
        menu.setPerm(DEFAULT_REPORT_VIEW);
        //menu.setComponent(webIndex);
        res += menuService.createMaster(menu,null);

        if(res == 0){
            throw new BusinessException(BusinessCode.CRUD_GENERAL_ERROR,"菜单生成失败");
        }

        /***      创建报表          **/
        meta.setMenuId(menu.getId());
        res += statisticsMetaService.createMaster(meta);

        /***      获取报表的id 重新更新菜单路由          **/
        String webIndex = pMenu.getComponent()+ File.separator+"table?id="+meta.getId();
        menu.setComponent(webIndex);
        menuService.updateMaster(menu,false);

        return res;
    }
}
