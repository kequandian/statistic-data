package com.jfeat;

import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.sql.SQLException;

/**
 * SpringBoot方式启动类
 *
 * @author Admin
 * @Date 2017/5/21 12:06
 */
@SpringBootApplication
@EnableScheduling
@MapperScan({"com.jfeat.**.dao", "com.jfeat.crud.**.mapper"})
public class AmApplication implements CommandLineRunner{

    protected final static Logger logger = LoggerFactory.getLogger(AmApplication.class);

    @Override
    public void run(String... strings) throws Exception {
    }

    public static void main(String[] args) throws SQLException {
        SpringApplication.run(AmApplication.class, args);
        logger.info("Statistic Statement is success!");
    }
}
