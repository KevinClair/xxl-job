package com.xxl.job.starter;

import com.xxl.job.core.util.IpUtil;
import com.xxl.job.core.util.NetUtil;
import com.xxl.job.starter.config.XxlJobAdminConfiguration;
import com.xxl.job.core.executor.config.XxlJobConfiguration;
import com.xxl.job.starter.config.XxlJobExecutorConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;

/**
 * xxl-job属性自动装配
 */
@Configuration
@EnableConfigurationProperties({XxlJobAdminConfiguration.class, XxlJobExecutorConfiguration.class})
@ConditionalOnProperty(prefix = "xxl-job.admin", value = "addresses")
public class XxlJobAutoConfiguration implements EnvironmentAware {

    private ConfigurableEnvironment environment;

    @Bean
    @ConditionalOnProperty(prefix = "xxl-job.executor", value = "address")
    public XxlJobConfiguration xxlJobExecutor(@NonNull XxlJobAdminConfiguration adminConfiguration, @NonNull XxlJobExecutorConfiguration executorConfiguration) {
        XxlJobConfiguration configuration = new XxlJobConfiguration();
        configuration.setAddress(adminConfiguration.getAddress());
        configuration.setAccessToken(adminConfiguration.getAccessToken());

        if (!StringUtils.hasLength(executorConfiguration.getAppName())) {
            String applicationName = environment.getProperty("spring.application.name");
            if (!StringUtils.hasLength(applicationName)) {
                throw new IllegalArgumentException("The name of executor is empty.");
            }
            configuration.setAppName(applicationName);
        } else {
            configuration.setAppName(executorConfiguration.getAppName());
        }

        if (!StringUtils.hasLength(executorConfiguration.getIp())) {
            configuration.setIp(IpUtil.getIp());
        } else {
            configuration.setIp(executorConfiguration.getIp());
        }
        configuration.setPort(executorConfiguration.getPort() > 0 ? executorConfiguration.getPort() : NetUtil.findAvailablePort(9999));
        configuration.setLogPath(executorConfiguration.getLogPath());
        configuration.setLogRetentionDays(executorConfiguration.getLogRetentionDays());
        return configuration;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = (ConfigurableEnvironment) environment;
    }
}
