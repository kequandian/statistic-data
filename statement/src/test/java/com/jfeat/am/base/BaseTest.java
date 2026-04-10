package com.jfeat.am.base;

import org.junit.jupiter.api.Test;

/**
 * Created by vincenthuang on 24/05/2018.
 */
public class BaseTest {
    @Test
    public void testSql(){
        String columnName = "status";
        String table = "user";
        String result = String.format("select distinct %s from %s\n" +
                "        where %s is not null", columnName, table, columnName);


        "".toString();
    }
}
