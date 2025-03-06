package com.jfeat.am.module.statistics.api;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfeat.common.HttpUtil;
import com.jfeat.poi.agent.PoiAgentExporter;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Created on 2020/11/12 2:05 下午.
 *
 * @author Wen Hao
 */
@Api("统计 [Statistics] 导入导出")
@RestController
@RequestMapping("/api/adm/stat/io")
public class StatisticsIoEndpoint {

    protected final static Logger logger = LoggerFactory.getLogger(StatisticsIoEndpoint.class);

    private static final String API_PREFIX = "/api/adm/stat/meta";

    @ApiOperation("根据字段导出报表")
    @GetMapping("/export/{field}")
    public void getConfigList(@PathVariable String field, HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, String[]> parameterMap = request.getParameterMap();
        logger.info("parameterMap --> {}", toPrintMap(parameterMap));
        response.setContentType("application/octet-stream");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=%s.xlsx", field));
        response.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION);
        response.getOutputStream().write(export(field).readAllBytes());
    }

    /**
     * 根据字段导出
     * @param field
     * @return
     */
    private ByteArrayInputStream export(String field) {
        // Authorization
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest httpRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
        String authorization = httpRequest.getHeader("Authorization");
        // api
        String apiPath = getApiPath(httpRequest, field);
        // process search
        apiPath = processSearch(apiPath, httpRequest);
        // process page size
        apiPath = processPageSize(apiPath, authorization);

        // 访问api 获取数据
        JSONObject data = HttpUtil.getResponse(apiPath, authorization).getJSONObject("data");
        // header
        List<String> header = data.getJSONArray("header").toJavaList(String.class);
        // rows jsonArray
        JSONArray rows = data.getJSONArray("rows");
        // rows map
        List<Map<String, String>> rowsMapList = getRowsMapList(rows);

        // export
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PoiAgentExporter.exportExcel(rowsMapList, header, header, baos);
        return new ByteArrayInputStream(baos.toByteArray());
    }

    private List<Map<String, String>> getRowsMapList(JSONArray rows) {
        List<Map<String, String>> rowsMapList = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            JSONObject obj = rows.getJSONObject(i);
            Map<String, String> rowMap = new HashMap<>();
            // 循环转换
            Iterator it = obj.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> entry = (Map.Entry<String, String>) it.next();
                rowMap.put(entry.getKey(), entry.getValue());
            }
            rowsMapList.add(rowMap);
        }
        return rowsMapList;
    }

    private String getApiPath(HttpServletRequest httpRequest, String field) {
        String requestURI = httpRequest.getRequestURI();
        StringBuffer requestURL = httpRequest.getRequestURL();
        // String requestURL = "http://cloud.biliya.cn/api/io/excel/xxxx";
        // String requestURI = "/api/io/excel/xxxx";
        int index = requestURL.indexOf(requestURI);
        return requestURL.substring(0, index) + API_PREFIX + "/" + field;
    }

    private String processSearch(String apiPath, HttpServletRequest request) {
        String queryString = request.getQueryString();
        return HttpUtil.setQueryParams(apiPath, queryString);
    }

    private String processPageSize(String apiPath, String authorization) {
        JSONObject data = HttpUtil.getResponse(apiPath, authorization).getJSONObject("data");
        String total = data.getString("total");
        // apiPath = HttpUtil.setQueryParam(apiPath,"pageNum", "1");
        return HttpUtil.setQueryParam(apiPath, "pageSize", total);
    }

    private Map<String, List<String>> toPrintMap(Map<String, String[]> parameterMap) {
        Map<String, List<String>> printMap = new HashMap<>(parameterMap.size());
        for (String key : parameterMap.keySet()) {
            printMap.put(key, Arrays.asList(parameterMap.get(key)));
        }
        return printMap;
    }
}
