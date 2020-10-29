package com.jfeat.am.module.statistics.api.model;

import com.jfeat.am.module.statistics.services.gen.persistence.model.StatisticsMetaGroup;

public class TemplateChildren {

    public TemplateChildren(StatisticsMetaGroup statisticsMetaGroup){
        this.presenter = statisticsMetaGroup.getPresenter();
        this.field = statisticsMetaGroup.getField();
        this.seq = statisticsMetaGroup.getSeq();

    }

    private Integer seq;
    //前端组件名
    private String presenter;
    //域
    private String field;




    public Integer getSeq() {
        return seq;
    }

    public void setSeq(Integer seq) {
        this.seq = seq;
    }

    public String getPresenter() {
        return presenter;
    }

    public void setPresenter(String presenter) {
        this.presenter = presenter;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }
}
