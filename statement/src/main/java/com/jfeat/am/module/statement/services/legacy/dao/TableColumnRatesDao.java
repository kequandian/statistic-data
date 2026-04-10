package com.jfeat.am.module.statement.services.legacy.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2017/8/9.
 */
@Deprecated
@Mapper
public interface TableColumnRatesDao extends BaseMapper {
    List<String> queryColumnValues(@Param("table") String table, @Param("column") String column);

    Map<String,Integer> getColumnRates(@Param("table") String table,@Param("column") String column,@Param("columnValues") List<String> columnValues,@Param("timeName") String timeName,@Param("startTime") String startTime,@Param("endTime") String endTime);

    Map<String,Integer> getColumnValueTotal(@Param("table") String table, @Param("column") String column, @Param("columnValue") String columnValue, @Param("timeName") String timeName, @Param("startTime") String startTime, @Param("endTime") String endTime);
}


