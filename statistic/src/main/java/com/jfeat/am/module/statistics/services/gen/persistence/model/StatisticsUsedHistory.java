package com.jfeat.am.module.statistics.services.gen.persistence.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author Code generator
 * @since 2022-08-31
 */
@TableName("st_statistics_used_history")
public class StatisticsUsedHistory extends Model<StatisticsUsedHistory> {

    private static final long serialVersionUID=1L;

      @TableId(value = "id", type = IdType.AUTO)
      private Long id;

    private Date createTime;

    private Date updateTime;

    private Long userId;

    private Long metaId;

    private String metaTitle;

    private String comeForm;

    private String comeFormType;

    
    public Long getId() {
        return id;
    }

      public StatisticsUsedHistory setId(Long id) {
          this.id = id;
          return this;
      }
    
    public Date getCreateTime() {
        return createTime;
    }

      public StatisticsUsedHistory setCreateTime(Date createTime) {
          this.createTime = createTime;
          return this;
      }
    
    public Date getUpdateTime() {
        return updateTime;
    }

      public StatisticsUsedHistory setUpdateTime(Date updateTime) {
          this.updateTime = updateTime;
          return this;
      }
    
    public Long getUserId() {
        return userId;
    }

      public StatisticsUsedHistory setUserId(Long userId) {
          this.userId = userId;
          return this;
      }
    
    public Long getMetaId() {
        return metaId;
    }

      public StatisticsUsedHistory setMetaId(Long metaId) {
          this.metaId = metaId;
          return this;
      }
    
    public String getMetaTitle() {
        return metaTitle;
    }

      public StatisticsUsedHistory setMetaTitle(String metaTitle) {
          this.metaTitle = metaTitle;
          return this;
      }
    
    public String getComeForm() {
        return comeForm;
    }

      public StatisticsUsedHistory setComeForm(String comeForm) {
          this.comeForm = comeForm;
          return this;
      }
    
    public String getComeFormType() {
        return comeFormType;
    }

      public StatisticsUsedHistory setComeFormType(String comeFormType) {
          this.comeFormType = comeFormType;
          return this;
      }

      public static final String ID = "id";

      public static final String CREATE_TIME = "create_time";

      public static final String UPDATE_TIME = "update_time";

      public static final String USER_ID = "user_id";

      public static final String META_ID = "meta_id";

      public static final String META_TITLE = "meta_title";

      public static final String COME_FORM = "come_form";

      public static final String COME_FORM_TYPE = "come_form_type";

      @Override
    public Serializable pkVal() {
          return this.id;
      }

    @Override
    public String toString() {
        return "StatisticsUsedHistory{" +
              "id=" + id +
                  ", createTime=" + createTime +
                  ", updateTime=" + updateTime +
                  ", userId=" + userId +
                  ", metaId=" + metaId +
                  ", metaTitle=" + metaTitle +
                  ", comeForm=" + comeForm +
                  ", comeFormType=" + comeFormType +
              "}";
    }
}
