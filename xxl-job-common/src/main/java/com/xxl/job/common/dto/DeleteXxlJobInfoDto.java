package com.xxl.job.common.dto;

import java.io.Serializable;

public class DeleteXxlJobInfoDto implements Serializable {

    /**
     * job的id
     */
    private int jobId;

    /**
     * job执行器名称
     */
    private String jobHandlerName;

    public DeleteXxlJobInfoDto(int jobId, String jobHandlerName) {
        this.jobId = jobId;
        this.jobHandlerName = jobHandlerName;
    }

    public DeleteXxlJobInfoDto() {
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public String getJobHandlerName() {
        return jobHandlerName;
    }

    public void setJobHandlerName(String jobHandlerName) {
        this.jobHandlerName = jobHandlerName;
    }
}
