package com.jfeat.am.module.statistic.mq;

import com.alibaba.fastjson2.JSON;
import com.jfeat.am.module.statistics.services.notify.StatisticNotifyData;
import com.jfeat.am.module.statistics.services.notify.StatisticsNotifyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.annotation.Resource;

/**
 * Created by Silent-Y on 2017/8/31.
 */
@Component
@RabbitListener(queues = Config.STATISTIC_DATA_NOTIFY)
public class StatisticNotifyReceiver {

    @Resource
    private StatisticsNotifyService statisticsNotifyService;

    private static final Logger logger = LoggerFactory.getLogger(StatisticNotifyReceiver.class);

    @RabbitHandler
    public void process(@Payload String message) {
        logger.info("Receiver : " + message);

        if (!StringUtils.hasText(message)) {
            return;
        }

        StatisticNotifyData memberAnalysisNotifyData = JSON.parseObject(message, StatisticNotifyData.class);
        statisticsNotifyService.insertStatisticRecord(memberAnalysisNotifyData);
    }
}
