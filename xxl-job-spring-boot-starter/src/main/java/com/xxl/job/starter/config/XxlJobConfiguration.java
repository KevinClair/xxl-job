package com.xxl.job.starter.config;

/**
 * xxl-job属性装配
 */
public class XxlJobConfiguration {

    private XxlJobAdminConfiguration admin;

    private XxlJobExecutorConfiguration executor;

    public XxlJobAdminConfiguration getAdmin() {
        return admin;
    }

    public void setAdmin(XxlJobAdminConfiguration admin) {
        this.admin = admin;
    }

    public XxlJobExecutorConfiguration getExecutor() {
        return executor;
    }

    public void setExecutor(XxlJobExecutorConfiguration executor) {
        this.executor = executor;
    }
}
