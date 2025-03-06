package com.jfeat.am.module.statistics.api;

import com.alibaba.fastjson.JSONObject;
import com.jfeat.am.module.statistics.api.model.MetaTag;
import com.jfeat.am.module.statistics.services.crud.ExtendedStatistics;
import com.jfeat.crud.base.tips.SuccessTip;
import com.jfeat.crud.base.tips.Tip;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;

@RequestMapping("/api/stat/user/audit")
public class UserAuditEndpoint {
    @Resource
    ExtendedStatistics extendedStatistics;


    @ApiOperation("根据字段获取报表")
    @GetMapping("/table")
    public Tip getConfigListByParam(
                                    @RequestParam(name = "id", required = false, defaultValue = "1")  Long id,
                                    @RequestParam(name = "pageNum", required = false, defaultValue = "1") Long current,
                                    @RequestParam(name = "pageSize", required = false, defaultValue = "10") Long size
    ) {
        MetaTag metaTag = new MetaTag();
        metaTag.setCurrent(current);
        metaTag.setSize(size);
        JSONObject data = extendedStatistics.getJSONById(id, metaTag);
        return SuccessTip.create(data);
    }

}
