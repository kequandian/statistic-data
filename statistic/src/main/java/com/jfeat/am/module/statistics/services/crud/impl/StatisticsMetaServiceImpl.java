package com.jfeat.am.module.statistics.services.crud.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.jfeat.am.core.jwt.JWTKit;
import com.jfeat.am.module.statistics.api.model.GenWebSetting;
import com.jfeat.am.module.statistics.api.model.MetaOutputSetting;
import com.jfeat.am.module.statistics.api.model.MetaTag;
import com.jfeat.am.module.statistics.services.crud.SQLSearchLabelService;
import com.jfeat.am.module.statistics.services.crud.StatisticsMetaService;
import com.jfeat.am.module.statistics.api.model.MetaColumns;
import com.jfeat.am.module.statistics.services.domain.model.StatisticsMetaRecord;
import com.jfeat.am.module.statistics.services.gen.crud.service.impl.CRUDStatisticsMetaServiceImpl;
import com.jfeat.am.module.statistics.services.gen.persistence.dao.StatisticsMetaMapper;
import com.jfeat.am.module.statistics.services.gen.persistence.model.StatisticsMeta;
import com.jfeat.am.module.statistics.util.GenCodeUtil;
import com.jfeat.crud.base.exception.BusinessCode;
import com.jfeat.crud.base.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.io.File;
import java.sql.*;
import java.util.*;

import static com.jfeat.am.module.statistics.api.perm.StatisticsMetaPermission.DEFAULT_REPORT_PERM_ID;
import static com.jfeat.am.module.statistics.api.perm.StatisticsMetaPermission.DEFAULT_REPORT_VIEW;

/**
 * <p>
 * implementation
 * </p>
 * CRUDStatisticsMetaService
 *
 * @author Code Generator
 * @since 2018-07-29
 */

@Service
public class StatisticsMetaServiceImpl extends CRUDStatisticsMetaServiceImpl implements StatisticsMetaService {

    @Resource
    DataSource dataSource;
    @Resource
    StatisticsMetaMapper statisticsMetaMapper;

    @Resource
    SQLSearchLabelService sqlSearchLabelService;

    @Resource
    GenWebSetting genWebSetting;


    protected final static Logger logger = LoggerFactory.getLogger(StatisticsMetaServiceImpl.class);

    @Override
    public StatisticsMeta getStatisticsMetaById(Long id){
        return statisticsMetaMapper.selectById(id);
    }



    @Override
    public String genWebCode(StatisticsMeta meta){
        String url = GenCodeUtil.genUrl(genWebSetting.getWebProject(), meta.getField());
        String indexFileName = meta.getField()+"Index.js";
        GenCodeUtil.genCode(url,indexFileName,GenCodeUtil.genIndexTemplate(meta).toString());
        GenCodeUtil.genCode(url,meta.getField()+".js",GenCodeUtil.genStringByMeta(meta).toString());
        return File.separator + meta.getField()+ File.separator + indexFileName;
    }


    @Override
    public String getQuerySql(String field, String recordName, String tuple, String cluster, String timeline) {
        return null;
    }

    //根据字段获取配置
    @Override
    public StatisticsMeta getStatisticsMetas(String field) {
        //根据field获取sql
        List<StatisticsMeta> statisticsMetas = statisticsMetaMapper.selectList(new QueryWrapper<StatisticsMeta>().eq(StatisticsMeta.FIELD, field));
        if (statisticsMetas != null && statisticsMetas.size() > 0) {
            StatisticsMeta meta = statisticsMetas.get(0);
            return meta;
        } else {
            throw new BusinessException(BusinessCode.CRUD_QUERY_FAILURE, "查找不到field对应的Meta");
        }

    }

    //根据field获取 json化的 表
    @Override
    public JSONObject getByField(String field, MetaTag metaTag) {

        HttpServletRequest request = ((ServletRequestAttributes) Objects
                .requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();

        String[] typeArray = null;
        String[] searchArray = null;
        JSONObject data = new JSONObject();
        StringBuilder sql = new StringBuilder();

        StatisticsMeta meta = getStatisticsMetas(field);

        //是否启用Type
        if (metaTag.isEnableType()) {
            if (meta.getType() == null || meta.getType().equals("")) {
                throw new BusinessException(BusinessCode.CRUD_QUERY_FAILURE, "需要搜索的类型未配置");
            }
            //分类
            typeArray = meta.getType().split(",");
            data.put("columns", typeArray);
        }

        //字段说明模块
        if (metaTag.isEnableTips()) {
            //字段的说明
            String tips = meta.getTips();
            //调用策略方法解包 tips
            JSONArray endtipArray = tipUnpacking(tips);
            data.put("tips", endtipArray);
        }

        if (metaTag.isEnableSearch()) {
            //搜索模块
            if (meta.getSearch() == null || meta.getSearch().equals("")) {
            } else {
                searchArray = meta.getSearch().split(",");
            }
            data.put("searchColumns", searchArray);
        }

        sql.append(meta.getQuerySql());
        data = getTableInfo(data, typeArray, sql, metaTag, "rows");

        return data;
    }


    //countSQL 用于查询总记录数
    @Override
    public JSONObject getTableInfo(JSONObject data, String[] typeArray, StringBuilder sql, MetaTag metaTag, String rowsName) {
        HttpServletRequest request = ((ServletRequestAttributes) Objects
                .requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();

        //整理sql执行
        Map<String, String[]> parameterMap = request.getParameterMap();
        //去除所有的‘;’防止拼接出错
        sql = new StringBuilder(sql.toString().replaceAll(";", ""));
        //使用标签
        sql = sqlSearchLabelService.getSQL(parameterMap, sql);

        String countSQL;
        //替换字符串 查找总数
        countSQL = sql.toString().replaceFirst("(select|SELECT)", "SELECT COUNT(*) as total,");

        //分页 //总页数 pages //每页大小 size
        //总记录数 //当前页数 current
        Long pages = 0L;
        Long total = 0L;
        List<JSONObject> objList = new ArrayList<>();

        Connection connection = null;
        Statement stmt = null;
        ResultSet rs = null;
        ResultSet rese = null;
        try {
            connection = dataSource.getConnection();
            List<String> names = new ArrayList<>();//字段名
            Map<String, String> nameTypeMap = new HashMap<>();//名字 类型映射
            //创建 可循环滚动的rs
            stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            //第一次运行读取字段
            rs = stmt.executeQuery(countSQL);
            ResultSetMetaData resultSetMetaData = rs.getMetaData();
            int colunmCount = resultSetMetaData.getColumnCount();

            if (metaTag.isEnableType()) {
                if (typeArray.length != colunmCount - 1) {
                    throw new BusinessException(BusinessCode.CRUD_QUERY_FAILURE, "得到的字段数量和类型数量不匹配 请检查sql和type");
                }
            }

            //循环获取 字段名
            for (int i = 0; i < colunmCount; i++) {
                String name = resultSetMetaData.getColumnLabel(i + 1);
                if (name.equals("total")) {
                } else {
                    names.add(name);
                    if (metaTag.isEnableType()) {
                        nameTypeMap.put(name, typeArray[i - 1]);
                    }
                }
            }

            // 添加搜索SQL-----------
            if (metaTag.isEnableType()) {
                sql = getSearchSQL(sql, request, nameTypeMap);
            }

            // 添加排序-----------
            //sql=orderSQL(sql,nameTypeMap);

            //加入分页SQL
            if (metaTag.isEnablePages()) {
                //重新搜索查找总数
                countSQL = sql.toString().replaceFirst("(select|SELECT)", "SELECT COUNT(*) as total,");
                rese = stmt.executeQuery(countSQL);
                //指针回到最初位置
                rese.beforeFirst();
                while (rese.next()) {
                    //使用sql查找总数据行数
                    total = Long.parseLong(rese.getObject("total").toString());
                    //向上取整
                    pages = (long) Math.ceil((double) total / (double) metaTag.getSize());
                }
                sql.append(" limit " + ((metaTag.getCurrent() - 1) * metaTag.getSize()) + "," + metaTag.getSize());
                rese.close();
            }

            System.out.println("最终执行的sql:    ");
            System.out.println(sql);
            //更改sql 查全部
            rs = stmt.executeQuery(sql.toString());
            while (rs.next()) {
                JSONObject pojoObject = new JSONObject();
                for (String name : names) {
                    //数据为空 则返回空
                    if (rs.getObject(name) != null) {
                        //单独处理百分比
                        if (metaTag.isEnableType() && MetaColumns.PERCENT.equals(nameTypeMap.get(name))) {
                            pojoObject.put(name, Float.parseFloat(rs.getObject(name).toString()) * 10000 / 100);
                        } else if (metaTag.isEnableType() && MetaColumns.JSON_OBJECT.equals(nameTypeMap.get(name))) {
                            //处理jsonObject
                            String objectString = rs.getString(name);
                            if (StringUtils.isEmpty(objectString)) {
                                pojoObject.put(name, objectString);
                            } else {
                                JSONObject parse = JSONObject.parseObject(objectString);
                                pojoObject.put(name, parse);
                            }
                        } else if (metaTag.isEnableType() &&
                                (MetaColumns.JSON_ARRAY.equals(nameTypeMap.get(name)) || MetaColumns.USER_ARRAY.equals(nameTypeMap.get(name)))
                        ) {
                            //处理jsonArray
                            String arrayString = rs.getString(name);
                            if (StringUtils.isEmpty(arrayString)) {
                                pojoObject.put(name, arrayString);
                            } else {
                                JSONArray parse = JSONArray.parseArray(arrayString);
                                pojoObject.put(name, parse);
                            }

                        } else {
                            pojoObject.put(name, rs.getObject(name).toString());
                        }
                    } else {
                        pojoObject.put(name, null);
                    }
                }
                objList.add(pojoObject);
            }

            //字段名
            if (metaTag.isEnableHead()) {
                data.put("header", names);
            }

        } catch (SQLException e) {
            throw new BusinessException(BusinessCode.CRUD_QUERY_FAILURE, e.getMessage());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (rese != null) {
                    rese.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception e) {
                logger.error("数据库连接池关闭异常 {}", e);
            }
        }

        //分页相关
        if (metaTag.isEnablePages()) {
            data.put("total", total);
            data.put("size", metaTag.getSize());
            data.put("pages", pages);
            data.put("current", metaTag.getCurrent());
        }
        //结果列表
        if (!StringUtils.isEmpty(rowsName)) {
            data.put(rowsName, objList);
        } else {
            data.put("rows", objList);
        }


        return data;
    }


    //拼接搜索sql
    public StringBuilder getSearchSQL(StringBuilder sql, HttpServletRequest request, Map<String, String> nameType) {


        sql.insert(0, "select t.* from(");
        sql.append(")t where 1=1 ");
        String fieldRequest = null;
        String fieldRequests[] = null;
        String field = null;
        String type = null;
        Iterator<String> iter = nameType.keySet().iterator();
        while (iter.hasNext()) {
            field = iter.next();
            type = nameType.get(field);
            if (type.equals(MetaColumns.STRING) || type.equals(MetaColumns.BIG_STRING)) {
                fieldRequest = request.getParameter(field);
                if (fieldRequest != null && fieldRequest.length() > 0) {
                    searchSQLByTypeAndField(sql, type, field, fieldRequest);
                }
            } else {
                fieldRequests = request.getParameterValues(field);
                if (fieldRequests != null && fieldRequests.length > 0) {
                    searchSQLByTypeAndFields(sql, type, field, fieldRequests);
                }
            }
        }
        return sql;
    }

    //其他类型 拼接sql
    public StringBuilder searchSQLByTypeAndFields(StringBuilder sql, String type, String field, String[] fieldRequests) {
        String searchSQLLeft = " ";
        String searchSQLRigtht = " ";
        if (type.equals(MetaColumns.TIME)) {
            searchSQLLeft = " TO_DAYS(";
            searchSQLRigtht = ") ";
        }
        if (fieldRequests[0] != "") {
            sql.append(" AND ");
            sql.append(searchSQLLeft);
            sql.append(field);
            sql.append(searchSQLRigtht);
            sql.append(" >= ");
            sql.append(searchSQLLeft);
            sql.append("'");
            sql.append(fieldRequests[0]);
            sql.append("'");
            sql.append(searchSQLRigtht);
        }
        if (fieldRequests[1] != "") {
            sql.append(" AND ");
            sql.append(searchSQLLeft);
            sql.append(field);
            sql.append(searchSQLRigtht);
            sql.append(" <= ");
            sql.append(searchSQLLeft);
            sql.append("'");
            sql.append(fieldRequests[1]);
            sql.append("'");
            sql.append(searchSQLRigtht);
        }
        return sql;
    }

    //字符串类型 拼接sql
    public StringBuilder searchSQLByTypeAndField(StringBuilder sql, String type, String field, String fieldRequest) {
        if (type.equals(MetaColumns.STRING) || type.equals(MetaColumns.BIG_STRING)) {
            sql.append(" AND ");
            sql.append(field);
            sql.append(" LIKE ");
            sql.append("'%");
            sql.append(fieldRequest);
            sql.append("%'");
        }
        return sql;
    }

    //排序
    public StringBuilder orderSQL(StringBuilder sql, Map<String, String> nameType) {
        StringBuilder orderSQL = new StringBuilder();
        String field = null;
        String type = null;
        Iterator<String> iter = nameType.keySet().iterator();
        while (iter.hasNext()) {
            field = iter.next();
            type = nameType.get(field);
            if (type.equals(MetaColumns.DECIMAL)) {
                if (orderSQL.toString() == null || orderSQL.toString().equals("")) {
                    orderSQL.append(" order by ");
                    orderSQL.append("`");
                    orderSQL.append(field);
                    orderSQL.append("`");
                    orderSQL.append(" desc ");
                } else {
                    orderSQL.append(",");
                    orderSQL.append("`");
                    orderSQL.append(field);
                    orderSQL.append("`");
                    orderSQL.append(" desc ");
                }
            }
        }
        sql.append(orderSQL);
        return sql;

    }


    //根据field获取 json化的 表
    @Override
    public String getSQLByField(String field, HttpServletRequest request) {
        String[] typeArray = null;
        StringBuilder sql = new StringBuilder();
        Statement stmt = null;
        ResultSet rs = null;
        Connection connection = null;
        try {
            String countSQL;//用于查询总记录数
            connection = dataSource.getConnection();
            //根据field获取sql
            List<StatisticsMeta> statisticsMetas = statisticsMetaMapper.selectList(new QueryWrapper<StatisticsMeta>().eq(StatisticsMeta.FIELD, field));
            if (statisticsMetas != null && statisticsMetas.size() > 0) {
                StatisticsMeta meta = statisticsMetas.get(0);
                typeArray = meta.getType().split(",");
                //替换字符串 查找总数
                countSQL = meta.getQuerySql().replaceFirst("(select|SELECT)", "SELECT COUNT(*) as total,");
                //去除所有的‘;’防止拼接出错
                sql.append(meta.getQuerySql().replaceAll(";", ""));
            } else {
                throw new BusinessException(BusinessCode.CRUD_QUERY_FAILURE, "查找不到field对应的Meta");
            }
            //创建 可循环滚动的rs
            stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            rs = stmt.executeQuery(countSQL);
            ResultSetMetaData resultSetMetaData = rs.getMetaData();
            int colunmCount = resultSetMetaData.getColumnCount();
            List<String> names = new ArrayList<>();//字段名
            Map<String, String> nameTypeMap = new HashMap<>();//名字 类型映射
            if (typeArray.length != colunmCount - 1) {
                throw new BusinessException(BusinessCode.CRUD_QUERY_FAILURE, "得到的字段数量和类型数量不匹配 请检查sql和type");
            }
            //循环获取 字段名
            for (int i = 0; i < colunmCount; i++) {
                String name = resultSetMetaData.getColumnLabel(i + 1);
                if (name.equals("total")) {
                } else {
                    names.add(name);
                    nameTypeMap.put(name, typeArray[i - 1]);
                }
            }
            // 添加搜索-----------
            sql = getSearchSQL(sql, request, nameTypeMap);
            // 添加排序-----------
            sql = orderSQL(sql, nameTypeMap);
            System.out.println("最终执行的sql:    ");
            System.out.println(sql);

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception e) {
                logger.error("数据库连接池关闭异常 {}", e);
            }
        }

        return sql.toString();
    }

    //将tips字段解包成数组形式
    private JSONArray tipUnpacking(String tips) {
        JSONArray endtipArray = new JSONArray();
        if (tips != null && !"".equals(tips)) {
            String[] tipArray = tips.split(",");
            for (String tip : tipArray) {
                JSONObject vk = new JSONObject();
                String[] split = tip.split(":");
                if (split.length != 2) {
                    throw new BusinessException(BusinessCode.CRUD_QUERY_FAILURE, "字段的说明格式有误，请按照num:value,num2:value2格式配置");
                }
                vk.put(split[0], split[1]);
                endtipArray.add(vk);
            }
        }
        return endtipArray;
    }

    //处理json文件，动态生成报表页面
    public JSONObject getOutputSetting(String field, String title) {
        String jsonStringSetting = MetaOutputSetting.JSON_STRING_SETTING;
        JSONObject jsonSetting = JSONObject.parseObject(jsonStringSetting);
        JSONObject records = jsonSetting.getJSONObject("records");
        records.put("title", title);
        JSONArray item = records.getJSONArray("items");
        JSONObject bodyJSON = (JSONObject) item.get(1);
        JSONObject config = bodyJSON.getJSONObject("config");
        JSONObject api = config.getJSONObject("API");
        StringBuffer listBuffer = new StringBuffer();
        listBuffer.append("/api/adm/stat/meta/");
        listBuffer.append(field);
        api.put("listAPI", listBuffer.toString());
        JSONArray actions = config.getJSONArray("actions");
        JSONObject actions_0 = (JSONObject) actions.get(0);
        JSONObject options = actions_0.getJSONObject("options");
        StringBuffer apiBuffer = new StringBuffer();
        apiBuffer.append("/api/io/export/");
        apiBuffer.append(field);
        options.put("API", apiBuffer.toString());
        return jsonSetting;
    }

}


