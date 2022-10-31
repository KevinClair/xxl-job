package com.xxl.job.starter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * admin配置
 */
@ConfigurationProperties(prefix = "xxl-job.admin")
public class XxlJobAdminConfiguration {

    private String addresses;

    private String accessToken;

    public String getAddresses() {
        return addresses;
    }

    public void setAddresses(String addresses) {
        this.addresses = addresses;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
