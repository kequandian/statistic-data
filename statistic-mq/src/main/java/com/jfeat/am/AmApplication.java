package com.jfeat.am;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * SpringBoot方式启动类
 *
 * @author Admin
 * @Date 2017/5/21 12:06
 */
@SpringBootApplication
@EnableScheduling
public class AmApplication implements WebMvcConfigurer {

    protected final static Logger logger = LoggerFactory.getLogger(AmApplication.class);


    public static void main(String[] args) {
        SpringApplication.run(AmApplication.class, args);
        logger.info("SB Statistic is success!");
    }
}
