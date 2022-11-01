package com.xxl.job.starter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * admin配置
 */
@ConfigurationProperties(prefix = "xxl-job.admin")
public class XxlJobAdminConfiguration {

    private String address;

    private String accessToken;

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
}
