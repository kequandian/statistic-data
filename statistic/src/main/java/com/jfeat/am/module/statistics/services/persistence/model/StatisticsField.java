package com.jfeat.am.module.statistics.services.persistence.model;

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
 * @since 2018-10-12
 */
@TableName("st_statistics_field")
public class StatisticsField extends Model<StatisticsField> {

    private static final long serialVersionUID = 1L;

	@TableId(value="id", type= IdType.AUTO)
	private Long id;
    /**
     * 数据域唯一标识符
     */
	private String field;
    /**
     * 所属分组ID
     */
	@TableField("group_id")
	private Long groupId;
    /**
     * 所属分组名称
     */
	@TableField("group_name")
	private String groupName;
    /**
     * 数据域名称
     */
	private String name;
    /**
     * 统计数据类型[Count,Rate,Tuple [Timeline,Cluster]]
     */
	private String pattern;
    /**
     * 标准组件图表名称
     */
	private String chart;
    /**
     * [属性]是否不可见
     */
	@TableField("attr_invisible")
	private Integer attrInvisible;
    /**
     * 是否实时查询[via meta]
     */
	@TableField("attr_runtime")
	private Integer attrRuntime;
    /**
     * [属性]所占布局跨列数
     */
	@TableField("attr_span")
	private Integer attrSpan;
    /**
     * [属性]排序号
     */
	@TableField("attr_index")
	private Integer attrIndex;


	public Long getId() {
		return id;
	}

	public StatisticsField setId(Long id) {
		this.id = id;
		return this;
	}

	public String getField() {
		return field;
	}

	public StatisticsField setField(String field) {
		this.field = field;
		return this;
	}

	public Long getGroupId() {
		return groupId;
	}

	public StatisticsField setGroupId(Long groupId) {
		this.groupId = groupId;
		return this;
	}

	public String getGroupName() {
		return groupName;
	}

	public StatisticsField setGroupName(String groupName) {
		this.groupName = groupName;
		return this;
	}

	public String getName() {
		return name;
	}

	public StatisticsField setName(String name) {
		this.name = name;
		return this;
	}

	public String getPattern() {
		return pattern;
	}

	public StatisticsField setPattern(String pattern) {
		this.pattern = pattern;
		return this;
	}

	public String getChart() {
		return chart;
	}

	public StatisticsField setChart(String chart) {
		this.chart = chart;
		return this;
	}

	public Integer getAttrInvisible() {
		return attrInvisible;
	}

	public StatisticsField setAttrInvisible(Integer attrInvisible) {
		this.attrInvisible = attrInvisible;
		return this;
	}

	public Integer getAttrRuntime() {
		return attrRuntime;
	}

	public StatisticsField setAttrRuntime(Integer attrRuntime) {
		this.attrRuntime = attrRuntime;
		return this;
	}

	public Integer getAttrSpan() {
		return attrSpan;
	}

	public StatisticsField setAttrSpan(Integer attrSpan) {
		this.attrSpan = attrSpan;
		return this;
	}

	public Integer getAttrIndex() {
		return attrIndex;
	}

	public StatisticsField setAttrIndex(Integer attrIndex) {
		this.attrIndex = attrIndex;
		return this;
	}

	public static final String ID = "id";

	public static final String FIELD = "field";

	public static final String GROUP_ID = "group_id";

	public static final String GROUP_NAME = "group_name";

	public static final String NAME = "name";

	public static final String PATTERN = "pattern";

	public static final String CHART = "chart";

	public static final String ATTR_INVISIBLE = "attr_invisible";

	public static final String ATTR_RUNTIME = "attr_runtime";

	public static final String ATTR_SPAN = "attr_span";

	public static final String ATTR_INDEX = "attr_index";

	@Override
	public Serializable pkVal() {
		return this.id;
	}

	@Override
	public String toString() {
		return "StatisticsField{" +
			"id=" + id +
			", field=" + field +
			", groupId=" + groupId +
			", groupName=" + groupName +
			", name=" + name +
			", pattern=" + pattern +
			", chart=" + chart +
			", attrInvisible=" + attrInvisible +
			", attrRuntime=" + attrRuntime +
			", attrSpan=" + attrSpan +
			", attrIndex=" + attrIndex +
			"}";
	}
}
