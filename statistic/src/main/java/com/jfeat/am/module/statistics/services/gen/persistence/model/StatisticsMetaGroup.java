package com.jfeat.am.module.statistics.services.gen.persistence.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author Code generator
 * @since 2020-10-27
 */
@TableName("st_statistics_meta_group")
public class StatisticsMetaGroup extends Model<StatisticsMetaGroup> {

    private static final long serialVersionUID=1L;

      @TableId(value = "id", type = IdType.AUTO)
      private Long id;

    private String name;

    private Long pid;

    private String pattern;

    private Integer span;

    private String layout;

    private String title;

    private String field;

    private String presenter;

    private Integer seq;



    private String apiReturn;

    public Integer getSeq() {
        return seq;
    }

    public void setSeq(Integer seq) {
        this.seq = seq;
    }

    public String getApiReturn() {
        return apiReturn;
    }

    public void setApiReturn(String apiReturn) {
        this.apiReturn = apiReturn;
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

    public Long getId() {
        return id;
    }

      public StatisticsMetaGroup setId(Long id) {
          this.id = id;
          return this;
      }
    
    public String getName() {
        return name;
    }

      public StatisticsMetaGroup setName(String name) {
          this.name = name;
          return this;
      }
    
    public Long getPid() {
        return pid;
    }

      public StatisticsMetaGroup setPid(Long pid) {
          this.pid = pid;
          return this;
      }
    
    public String getPattern() {
        return pattern;
    }

      public StatisticsMetaGroup setPattern(String pattern) {
          this.pattern = pattern;
          return this;
      }
    
    public Integer getSpan() {
        return span;
    }

      public StatisticsMetaGroup setSpan(Integer span) {
          this.span = span;
          return this;
      }
    
    public String getLayout() {
        return layout;
    }

      public StatisticsMetaGroup setLayout(String layout) {
          this.layout = layout;
          return this;
      }
    
    public String getTitle() {
        return title;
    }

      public StatisticsMetaGroup setTitle(String title) {
          this.title = title;
          return this;
      }

      public static final String ID = "id";

      public static final String NAME = "name";

      public static final String PID = "pid";

      public static final String PATTERN = "pattern";

      public static final String SPAN = "span";

      public static final String LAYOUT = "layout";

      public static final String TITLE = "title";

      @Override
    public Serializable pkVal() {
          return this.id;
      }

    @Override
    public String toString() {
        return "StatisticsMetaGroup{" +
              "id=" + id +
                  ", name=" + name +
                  ", pid=" + pid +
                  ", pattern=" + pattern +
                  ", span=" + span +
                  ", layout=" + layout +
                  ", title=" + title +
              "}";
    }
}
