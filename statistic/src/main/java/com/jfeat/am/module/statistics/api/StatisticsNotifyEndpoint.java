package com.jfeat.am.module.statistics.api;

import com.jfeat.am.module.statistics.services.notify.StatisticNotifyData;
import com.jfeat.am.module.statistics.services.notify.StatisticsNotifyService;
import com.jfeat.crud.base.tips.SuccessTip;
import com.jfeat.crud.base.tips.Tip;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

/**
 * 统计数据通知端点
 * 用于插入统计数据记录
 */
@Api("统计 [Statistics] Notify")
@RestController
@RequestMapping("/api/adm/stat/notify")
public class StatisticsNotifyEndpoint {

    @Resource
    private StatisticsNotifyService statisticsNotifyService;

    @PostMapping
    @ApiOperation("插入统计数据记录")
    public Tip insertStatisticRecord(@RequestBody StatisticNotifyData data) {
        boolean success = statisticsNotifyService.insertStatisticRecord(data);
        return SuccessTip.create(success);
    }
}
