package com.jfeat.am.module.statistics.services.gen.persistence.model;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author Code Generator
 * @since 2020-04-17
 */
@TableName("st_statistics_meta")
public class StatisticsMeta extends Model<StatisticsMeta> {

    @TableField(exist = false)
    private com.alibaba.fastjson2.JSONObject extra;

    public com.alibaba.fastjson2.JSONObject getExtra() {
        return extra;
    }
    public void setExtra(com.alibaba.fastjson2.JSONObject extra) {
        this.extra = extra;
    }


    private static final long serialVersionUID = 1L;

	@TableId(value="id", type= IdType.AUTO)
	private Long id;
    /**
     * 数据指标唯一标识
     */
	private String field;
    /**
     * 实时查询sql
     */
	@TableField("query_sql")
	private String querySql;
    /**
     * 是否显示为百分比
     */
	private Integer percent;
    /**
     * 图标路径
     */
	private String icon;
    /**
     * 图表标题
     */
	private String title;
    /**
     * 字段类型
     */
	private String type;
    /**
     * 搜索字段
     */
	private String search;
    /**
     * 权限
     */
	private String permission;

	private String layout;

	private Integer span;

	private String tips;

	private String chart;

	private String pattern;
	
	private Long menuId;

	private Long permId;

	private String appid;

	private Integer endUserType;

	public String getAppid() {
		return appid;
	}

	public void setAppid(String appid) {
		this.appid = appid;
	}

	public Integer getEndUserType() {
		return endUserType;
	}

	public void setEndUserType(Integer endUserType) {
		this.endUserType = endUserType;
	}

	public Long getPermId() {
		return permId;
	}

	public void setPermId(Long permId) {
		this.permId = permId;
	}

	public Long getMenuId() {
		return menuId;
	}

	public void setMenuId(Long menuId) {
		this.menuId = menuId;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public String getChart() {
		return chart;
	}

	public void setChart(String chart) {
		this.chart = chart;
	}

	public Integer getSpan() {
		return span;
	}

	public void setSpan(Integer span) {
		this.span = span;
	}

	public String getLayout() {
		return layout;
	}

	public void setLayout(String layout) {
		this.layout = layout;
	}

	public String getTips() {
		return tips;
	}

	public void setTips(String tips) {
		this.tips = tips;
	}

	public Long getId() {
		return id;
	}

	public StatisticsMeta setId(Long id) {
		this.id = id;
		return this;
	}

	public String getField() {
		return field;
	}

	public StatisticsMeta setField(String field) {
		this.field = field;
		return this;
	}

	public String getQuerySql() {
		return querySql;
	}

	public StatisticsMeta setQuerySql(String querySql) {
		this.querySql = querySql;
		return this;
	}

	public Integer getPercent() {
		return percent;
	}

	public StatisticsMeta setPercent(Integer percent) {
		this.percent = percent;
		return this;
	}

	public String getIcon() {
		return icon;
	}

	public StatisticsMeta setIcon(String icon) {
		this.icon = icon;
		return this;
	}

	public String getTitle() {
		return title;
	}

	public StatisticsMeta setTitle(String title) {
		this.title = title;
		return this;
	}

	public String getType() {
		return type;
	}

	public StatisticsMeta setType(String type) {
		this.type = type;
		return this;
	}

	public String getSearch() {
		return search;
	}

	public StatisticsMeta setSearch(String search) {
		this.search = search;
		return this;
	}

	public String getPermission() {
		return permission;
	}

	public StatisticsMeta setPermission(String permission) {
		this.permission = permission;
		return this;
	}

	public static final String ID = "id";

	public static final String FIELD = "field";

	public static final String QUERY_SQL = "query_sql";

	public static final String PERCENT = "percent";

	public static final String ICON = "icon";

	public static final String TITLE = "title";

	public static final String TYPE = "type";

	public static final String SEARCH = "search";

	public static final String PERMISSION = "permission";

	@Override
	public Serializable pkVal() {
		return this.id;
	}

	@Override
	public String toString() {
		return "StatisticsMeta{" +
			"id=" + id +
			", field=" + field +
			", querySql=" + querySql +
			", percent=" + percent +
			", icon=" + icon +
			", title=" + title +
			", type=" + type +
			", search=" + search +
			", permission=" + permission +
			"}";
	}
}
