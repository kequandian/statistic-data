package com.jfeat.am.module.statement.services.statistics.route;

import com.alibaba.fastjson2.JSON;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Silent-Y on 2017/8/31.
 */
public class StatisticRouteData {

    /**
     * 名称(这里指统计域 域名）
     */
    private String name;

    /**
     * 记录时间
     */
    private Date recordTime;

    /**
     * 关于域名的所有统计数据项
     */
    private List<StatisticChunk> chunks;

    public StatisticRouteData addChunk(StatisticChunk chunk){
        if(chunks==null){
            chunks = new ArrayList<>();
        }

        chunks.add(chunk);

        return this;
    }

    public StatisticRouteData append(StatisticRouteData route){
        if(chunks==null){
            chunks = new ArrayList<>();
        }

        chunks.addAll(route.getChunks());

        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getRecordTime() {
        return recordTime;
    }

    public void setRecordTime(Date recordTime) {
        this.recordTime = recordTime;
    }

    public List<StatisticChunk> getChunks() {
        return chunks;
    }

    public void setChunks(List<StatisticChunk> chunks) {
        this.chunks = chunks;
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
