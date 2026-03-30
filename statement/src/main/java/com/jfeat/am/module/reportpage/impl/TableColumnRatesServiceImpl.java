package com.jfeat.am.module.reportpage.impl;
import com.jfeat.am.module.statement.services.legacy.dao.TableColumnRatesDao;
import com.jfeat.am.module.reportpage.TableColumnRatesService;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * Created by Silent-Y on 2017/11/6.
 */
@Deprecated
@Service
public class TableColumnRatesServiceImpl implements TableColumnRatesService {

    @Resource
    private TableColumnRatesDao tableColumnRatesDao;

    /*饼状图数据结构
    "titile":"饼状图数据结构",
    "timestamp":"数据生成时间",
    "type":"数据图描述类型",
    "data":[
    {value:335, name:'直接访问'},
    {value:310, name:'邮件营销'},
    {value:274, name:'联盟广告'},
    {value:235, name:'视频广告'},
    {value:400, name:'搜索引擎'}
    ]*/

    /**
     * 返回某时间段内某表所有值的各数量
     * @param table
     * @param column
     * @param timeName
     * @param startTime
     * @param endTime
     * @return
     */
    @Override
    public Map<String, Integer> getColumnRates(String table, String column, String timeName, String startTime, String endTime) {
        List<String> columnValues = tableColumnRatesDao.queryColumnValues(table, column);
        return tableColumnRatesDao.getColumnRates(table, column, columnValues, timeName, startTime, endTime);
    }

    /**
     * 返回时间段内表列某值的数量
     * @param table
     * @param column
     * @param columnValue
     * @param timeName
     * @param startTime
     * @param endTime
     * @return
     */
    @Override
    public Map<String, Integer> getColumnValueTotal(String table, String column, String columnValue, String timeName, String startTime, String endTime) {
        return tableColumnRatesDao.getColumnValueTotal(table, column, columnValue, timeName, startTime, endTime);
    }
}
