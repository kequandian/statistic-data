package com.jfeat.am.module.statement.services.leagcy.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * Created by Administrator on 2017/8/9.
 */
@Deprecated
public interface TableColumnStatusNumDao extends BaseMapper {

    Map<String,Integer> getColumnStatusNum(@Param("table") String table, @Param("column") String column, @Param("columnValue") String columnValue, @Param("timeName") String timeName, @Param("startTime") String startTime, @Param("endTime") String endTime);


}

