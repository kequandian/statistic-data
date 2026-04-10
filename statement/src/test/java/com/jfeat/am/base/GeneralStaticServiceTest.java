package com.jfeat.am.base;

import com.jfeat.AmApplication;
import com.jfeat.am.module.statement.services.statistics.*;
import com.jfeat.am.module.statement.services.statistics.route.StatisticRouteData;
import com.jfeat.am.module.statement.services.statistics.service.GeneralStatisticService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import org.slf4j.Logger;

@ActiveProfiles(profiles = "test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = AmApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration
@Transactional
public class GeneralStaticServiceTest {

    Logger logger = LoggerFactory.getLogger(GeneralStaticServiceTest.class.getSimpleName());

    @Autowired
    private GeneralStatisticService generalStatisticService;

    @Test
    public void testQueryStatisticPlaceholder() throws Exception{
        logger.info("testQueryStatisticPlaceholder...");
    }

    //@Test
    public void testQueryStatisticTotal() throws Exception {
        String name = "test";
        String sql = "select sum(case when sex=0 then 1 else 0 end) as '男' from t_test_staff";
        Statistics subject = generalStatisticService.queryStatistic(name,sql);
        StatisticRouteData statisticRouteData = subject.toRouteData();
        System.out.println(statisticRouteData);
    }

    //@Test
    public void testQueryStatisticRate() throws SQLException {
        String name = "test";
        String sql = "select sum(case when sex=0 then 1 else 0 end) as '男', sum(case when sex=1 then 1 else 0 end) as '女' from t_test_staff";
        StatisticRate statisticRate = generalStatisticService.queryStatisticRate(name, sql);
        StatisticRouteData statisticRouteData = statisticRate.toRouteData();
        System.out.println(statisticRouteData);
    }

    //@Test
    public void testQueryStatisticTuple() throws SQLException {
        String mame = "test";
        String sql = "select id,name,sex from t_staff";
        StatisticTuple statisticTuple = generalStatisticService.queryStatisticTuple(mame, sql);
        StatisticRouteData statisticRouteData = statisticTuple.toRouteData();
        System.out.println(statisticRouteData);
    }

    //@Test
    public void testQueryStatisticTotalTimeline() throws SQLException {
        String name = "test";
        String sql = "select sum(case when sex=0 then 1 else 0 end) as '男' from t_test_staff";

        Timeline tl_year = new Timeline(Timeline.Timelines.Y.toString(), "create_time");
        Timeline tl_month = new Timeline(Timeline.Timelines.M.toString(), "create_time");

        StatisticTimeline statisticTimeline =
                generalStatisticService.queryStatisticTimeline(name, sql, tl_year, tl_month);
        StatisticRouteData statisticRouteData = statisticTimeline.toRouteData();
        System.out.println(statisticRouteData);
    }

    //@Test
    public void testQueryStatisticRateTimeline() throws SQLException {
        String name = "test";
        String sql = "select sum(case when sex=0 then 1 else 0 end) as '男', sum(case when sex=1 then 1 else 0 end) as '女' from t_staff";

        Timeline tl_year = new Timeline(Timeline.Timelines.Y.toString(), "create_time");
        Timeline tl_month = new Timeline(Timeline.Timelines.M.toString(), "create_time");

        StatisticTimeline statisticTimeline = generalStatisticService.queryStatisticTimeline(name, sql, tl_year, tl_month);
        StatisticRouteData statisticRouteData = statisticTimeline.toRouteData();
        System.out.println(statisticRouteData);
    }

    //@Test
    public void testQueryStatisticTupleTimeline() throws SQLException {
        //TODO, tuple name FROM sql ?
        String name = "test";
        String sql = "select id,name,sex from t_test_staff";

        Timeline tl_year = new Timeline(Timeline.Timelines.Y.toString(), "create_time");
        Timeline tl_month = new Timeline(Timeline.Timelines.M.toString(), "create_time");

        StatisticTimeline statisticTimeline = generalStatisticService.queryStatisticTimeline(name, sql, tl_year, tl_month);
        if(statisticTimeline!=null) {
            StatisticRouteData statisticRouteData = statisticTimeline.toRouteData();
            System.out.println(statisticRouteData);
        }
    }
}
