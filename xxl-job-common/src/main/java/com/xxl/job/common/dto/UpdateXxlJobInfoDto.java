package com.xxl.job.common.dto;

import com.xxl.job.common.enums.ScheduleTypeEnum;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 应用端向admin主动修改job信息
 */
public class UpdateXxlJobInfoDto implements Serializable {

    // 执行器名称
    @NotNull
    private Integer jobId;

	// 调度类型,默认为corn
	private ScheduleTypeEnum scheduleType;
	// 调度配置，值含义取决于调度类型。如果是corn类型，此处填写corn表达式
	private String scheduleConf;

	// 调度状态：0-停止，1-运行
	private Integer triggerStatus;
	// 下次调度时间
	private long triggerNextTime;

	public Integer getJobId() {
		return jobId;
	}

	public void setJobId(Integer jobId) {
		this.jobId = jobId;
	}

	public ScheduleTypeEnum getScheduleType() {
		return scheduleType;
	}

	public void setScheduleType(ScheduleTypeEnum scheduleType) {
		this.scheduleType = scheduleType;
	}

	public String getScheduleConf() {
		return scheduleConf;
	}

	public void setScheduleConf(String scheduleConf) {
		this.scheduleConf = scheduleConf;
	}

	public Integer getTriggerStatus() {
		return triggerStatus;
	}

	public void setTriggerStatus(Integer triggerStatus) {
		this.triggerStatus = triggerStatus;
	}

	public long getTriggerNextTime() {
		return triggerNextTime;
	}

	public void setTriggerNextTime(long triggerNextTime) {
		this.triggerNextTime = triggerNextTime;
	}
}
