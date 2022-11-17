package com.xxl.job.core.executor.config;

/**
 * xxl-job属性装配
 */
public class XxlJobConfiguration {

    private String address;

    private String accessToken;

    private String appName;

    private String ip;

    private int port;

    private String executorAddress;

    private String logPath;

    private int logRetentionDays;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

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

    /**
     * Gets the value of executorAddress.
     *
     * @return the value of executorAddress
     */
    public String getExecutorAddress() {
        return executorAddress;
    }

    /**
     * Sets the executorAddress.
     *
     * @param executorAddress executorAddress
     */
    public void setExecutorAddress(final String executorAddress) {
        this.executorAddress = executorAddress;
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
