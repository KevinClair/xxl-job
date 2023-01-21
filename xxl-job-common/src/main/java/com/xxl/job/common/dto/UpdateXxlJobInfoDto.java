package com.xxl.job.common.dto;

import com.xxl.job.common.enums.ExecutorBlockStrategyEnum;
import com.xxl.job.common.enums.ExecutorRouteStrategyEnum;
import com.xxl.job.common.enums.MisfireStrategyEnum;
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

	// 调度任务描述
	private String jobDesc;
	// 负责人
	private String author;
    // 报警邮件X
	private String alarmEmail;

	// 调度类型,默认为corn
	private ScheduleTypeEnum scheduleType = ScheduleTypeEnum.CRON;
	// 调度配置，值含义取决于调度类型。如果是corn类型，此处填写corn表达式
	private String scheduleConf;

	// 执行器，JobHandler名称
	private String executorHandler;
	// 执行器，任务参数
	private String executorParam;

	// 执行器路由策略,默认为第一个
	private ExecutorRouteStrategyEnum executorRouteStrategy = ExecutorRouteStrategyEnum.FIRST;
	// 子任务ID，多个逗号分隔
	private String childJobId;
	// 调度过期策略。默认为忽略
	private MisfireStrategyEnum misfireStrategy = MisfireStrategyEnum.DO_NOTHING;
	// 阻塞处理策略，默认为单机串行
	private ExecutorBlockStrategyEnum executorBlockStrategy = ExecutorBlockStrategyEnum.SERIAL_EXECUTION;
	// 任务执行超时时间，单位秒
	private int executorTimeout = 0;
	// 失败重试次数
	private int executorFailRetryCount = 0;

	// 调度状态：0-停止，1-运行
	private int triggerStatus;
	// 下次调度时间
	private long triggerNextTime;

    public Integer getJobId() {
        return jobId;
    }

    public void setJobId(Integer jobId) {
        this.jobId = jobId;
    }

	public String getJobDesc() {
		return jobDesc;
	}

	public void setJobDesc(String jobDesc) {
		this.jobDesc = jobDesc;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getAlarmEmail() {
		return alarmEmail;
	}

	public void setAlarmEmail(String alarmEmail) {
		this.alarmEmail = alarmEmail;
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

	public String getExecutorHandler() {
		return executorHandler;
	}

	public void setExecutorHandler(String executorHandler) {
		this.executorHandler = executorHandler;
	}

	public String getExecutorParam() {
		return executorParam;
	}

	public void setExecutorParam(String executorParam) {
		this.executorParam = executorParam;
	}

	public ExecutorRouteStrategyEnum getExecutorRouteStrategy() {
		return executorRouteStrategy;
	}

	public void setExecutorRouteStrategy(ExecutorRouteStrategyEnum executorRouteStrategy) {
		this.executorRouteStrategy = executorRouteStrategy;
	}

	public String getChildJobId() {
		return childJobId;
	}

	public void setChildJobId(String childJobId) {
		this.childJobId = childJobId;
	}

	public MisfireStrategyEnum getMisfireStrategy() {
		return misfireStrategy;
	}

	public void setMisfireStrategy(MisfireStrategyEnum misfireStrategy) {
		this.misfireStrategy = misfireStrategy;
	}

	public ExecutorBlockStrategyEnum getExecutorBlockStrategy() {
		return executorBlockStrategy;
	}

	public void setExecutorBlockStrategy(ExecutorBlockStrategyEnum executorBlockStrategy) {
		this.executorBlockStrategy = executorBlockStrategy;
	}

	public int getExecutorTimeout() {
		return executorTimeout;
	}

	public void setExecutorTimeout(int executorTimeout) {
		this.executorTimeout = executorTimeout;
	}

	public int getExecutorFailRetryCount() {
		return executorFailRetryCount;
	}

	public void setExecutorFailRetryCount(int executorFailRetryCount) {
		this.executorFailRetryCount = executorFailRetryCount;
	}

	public int getTriggerStatus() {
		return triggerStatus;
	}

	public void setTriggerStatus(int triggerStatus) {
		this.triggerStatus = triggerStatus;
	}

	public long getTriggerNextTime() {
		return triggerNextTime;
	}

	public void setTriggerNextTime(long triggerNextTime) {
		this.triggerNextTime = triggerNextTime;
	}
}
