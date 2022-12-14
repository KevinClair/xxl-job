package com.xxl.job.starter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 执行器配置
 */
@ConfigurationProperties(prefix = "xxl-job.executor")
public class XxlJobExecutorConfiguration {

    private String appName;

    private String ip;

    private int port = 9999;

    private String logPath = "/data/applogs/xxl-job/jobhandler";

    private int logRetentionDays = 30;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public int getLogRetentionDays() {
        return logRetentionDays;
    }

    public void setLogRetentionDays(int logRetentionDays) {
        this.logRetentionDays = logRetentionDays;
    }
}
