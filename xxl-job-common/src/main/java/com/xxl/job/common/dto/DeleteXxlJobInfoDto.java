package com.xxl.job.common.dto;

import org.springframework.util.StringUtils;

import javax.validation.constraints.AssertTrue;
import java.io.Serializable;
import java.util.Objects;

public class DeleteXxlJobInfoDto implements Serializable {

    /**
     * job的id
     */
    private Integer jobId;

    /**
     * job执行器名称
     */
    private String jobHandlerName;

    public DeleteXxlJobInfoDto() {
    }

    public DeleteXxlJobInfoDto(Integer jobId, String jobHandlerName) {
        this.jobId = jobId;
        this.jobHandlerName = jobHandlerName;
    }

    public Integer getJobId() {
        return jobId;
    }

    public void setJobId(Integer jobId) {
        this.jobId = jobId;
    }

    public String getJobHandlerName() {
        return jobHandlerName;
    }

    public void setJobHandlerName(String jobHandlerName) {
        this.jobHandlerName = jobHandlerName;
    }

    @AssertTrue(message = "jobId和jobHandlerName不能同事为空")
    public Boolean check() {
        if (!StringUtils.hasLength(jobHandlerName) && Objects.isNull(jobId)) {
            return false;
        }
        return true;
    }
}
