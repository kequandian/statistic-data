package com.jfeat.am.module.statistics.api.model;

import com.alibaba.fastjson2.JSONObject;

import java.util.List;

public class MetaGroupTemplate {
    private String title;
    private Integer span;
    private JSONObject layout;
    private List<TemplateChildren> children;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getSpan() {
        return span;
    }

    public void setSpan(Integer span) {
        this.span = span;
    }

    public JSONObject getLayout() {
        return layout;
    }

    public void setLayout(JSONObject layout) {
        this.layout = layout;
    }

    public List<TemplateChildren> getChildren() {
        return children;
    }

    public void setChildren(List<TemplateChildren> children) {
        this.children = children;
    }
}
