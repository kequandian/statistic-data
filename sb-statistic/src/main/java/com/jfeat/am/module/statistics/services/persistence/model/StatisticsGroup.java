package com.jfeat.am.module.statistics.services.persistence.model;

import java.io.Serializable;

import com.baomidou.mybatisplus.activerecord.Model;
import com.baomidou.mybatisplus.annotations.TableName;
import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author Code Generator
 * @since 2018-02-26
 */
@TableName("st_statistics_group")
public class StatisticsGroup extends Model<StatisticsGroup> {

    private static final long serialVersionUID = 1L;

	private Long id;
    /**
     * 组显示名称
     */
	private String name;
    /**
     * 组标识
     */
	private String identifier;
    /**
     * 上级分组
     */
	private Long pid;
    /**
     * 分组描述
     */
	private String description;
    /**
     * 排序号
     */
	private Integer index;
    /**
     * 图表名称[Pie,Chain]环比
     */
	private String chart;


	public Long getId() {
		return id;
	}

	public StatisticsGroup setId(Long id) {
		this.id = id;
		return this;
	}

	public String getName() {
		return name;
	}

	public StatisticsGroup setName(String name) {
		this.name = name;
		return this;
	}

	public String getIdentifier() {
		return identifier;
	}

	public StatisticsGroup setIdentifier(String identifier) {
		this.identifier = identifier;
		return this;
	}

	public Long getPid() {
		return pid;
	}

	public StatisticsGroup setPid(Long pid) {
		this.pid = pid;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public StatisticsGroup setDescription(String description) {
		this.description = description;
		return this;
	}

	public Integer getIndex() {
		return index;
	}

	public StatisticsGroup setIndex(Integer index) {
		this.index = index;
		return this;
	}

	public String getChart() {
		return chart;
	}

	public StatisticsGroup setChart(String chart) {
		this.chart = chart;
		return this;
	}

	public static final String ID = "id";

	public static final String NAME = "name";

	public static final String IDENTIFIER = "identifier";

	public static final String PID = "pid";

	public static final String DESCRIPTION = "description";

	public static final String INDEX = "index";

	public static final String CHART = "chart";

	@Override
	protected Serializable pkVal() {
		return this.id;
	}

	@Override
	public String toString() {
		return "StatisticsGroup{" +
			"id=" + id +
			", name=" + name +
			", identifier=" + identifier +
			", pid=" + pid +
			", description=" + description +
			", index=" + index +
			", chart=" + chart +
			"}";
	}
}
