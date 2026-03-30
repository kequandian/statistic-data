package com.jfeat.am.module.statistics.services.persistence.model;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
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
@TableName("st_statistics_record")
public class StatisticsRecord extends Model<StatisticsRecord> {

    private static final long serialVersionUID = 1L;

	@TableId(value="id", type= IdType.AUTO)
	private Long id;
    /**
     * 数据域标识符
     */
	private String field;
    /**
     * 统计归属标识
     */
	private String identifier;
    /**
     * 记录排序号
     */
	private Integer seq;
    /**
     * 记录名称
     */
	@TableField("record_name")
	private String recordName;
    /**
     * 记录值
     */
	@TableField("record_value")
	private String recordValue;
    /**
     * 记录值所属行名称
     */
	@TableField("record_tuple")
	private String recordTuple;
    /**
     * 记录值所属分类名称
     */
	@TableField("record_cluster")
	private String recordCluster;
    /**
     * 记录值所属时间区间名称
     */
	@TableField("record_timeline")
	private String recordTimeline;
    /**
     * 统计时段说明[T,D,W,M,Y,LD3,LW1,LM1,LM3,Q1,Q2,Q3,Q4,TF]
     */
	private String timeline;
    /**
     * 记录创建时间
     */
	@TableField("create_time")
	private Date createTime;
    /**
     * 临时标记数据域ID
     */
	@TableField("tmp_field_id")
	private Long tmpFieldId;


	public Long getId() {
		return id;
	}

	public StatisticsRecord setId(Long id) {
		this.id = id;
		return this;
	}

	public String getField() {
		return field;
	}

	public StatisticsRecord setField(String field) {
		this.field = field;
		return this;
	}

	public String getIdentifier() {
		return identifier;
	}

	public StatisticsRecord setIdentifier(String identifier) {
		this.identifier = identifier;
		return this;
	}

	public Integer getSeq() {
		return seq;
	}

	public StatisticsRecord setSeq(Integer seq) {
		this.seq = seq;
		return this;
	}

	public String getRecordName() {
		return recordName;
	}

	public StatisticsRecord setRecordName(String recordName) {
		this.recordName = recordName;
		return this;
	}

	public String getRecordValue() {
		return recordValue;
	}

	public StatisticsRecord setRecordValue(String recordValue) {
		this.recordValue = recordValue;
		return this;
	}

	public String getRecordTuple() {
		return recordTuple;
	}

	public StatisticsRecord setRecordTuple(String recordTuple) {
		this.recordTuple = recordTuple;
		return this;
	}

	public String getRecordCluster() {
		return recordCluster;
	}

	public StatisticsRecord setRecordCluster(String recordCluster) {
		this.recordCluster = recordCluster;
		return this;
	}

	public String getRecordTimeline() {
		return recordTimeline;
	}

	public StatisticsRecord setRecordTimeline(String recordTimeline) {
		this.recordTimeline = recordTimeline;
		return this;
	}

	public String getTimeline() {
		return timeline;
	}

	public StatisticsRecord setTimeline(String timeline) {
		this.timeline = timeline;
		return this;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public StatisticsRecord setCreateTime(Date createTime) {
		this.createTime = createTime;
		return this;
	}

	public Long getTmpFieldId() {
		return tmpFieldId;
	}

	public StatisticsRecord setTmpFieldId(Long tmpFieldId) {
		this.tmpFieldId = tmpFieldId;
		return this;
	}

	public static final String ID = "id";

	public static final String FIELD = "field";

	public static final String IDENTIFIER = "identifier";

	public static final String SEQ = "seq";

	public static final String RECORD_NAME = "record_name";

	public static final String RECORD_VALUE = "record_value";

	public static final String RECORD_TUPLE = "record_tuple";

	public static final String RECORD_CLUSTER = "record_cluster";

	public static final String RECORD_TIMELINE = "record_timeline";

	public static final String TIMELINE = "timeline";

	public static final String CREATE_TIME = "create_time";

	public static final String TMP_FIELD_ID = "tmp_field_id";

	@Override
	public Serializable pkVal() {
		return this.id;
	}

	@Override
	public String toString() {
		return "StatisticsRecord{" +
			"id=" + id +
			", field=" + field +
			", identifier=" + identifier +
			", seq=" + seq +
			", recordName=" + recordName +
			", recordValue=" + recordValue +
			", recordTuple=" + recordTuple +
			", recordCluster=" + recordCluster +
			", recordTimeline=" + recordTimeline +
			", timeline=" + timeline +
			", createTime=" + createTime +
			", tmpFieldId=" + tmpFieldId +
			"}";
	}
}
