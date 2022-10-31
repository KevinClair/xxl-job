package com.xxl.job.starter;

import com.xxl.job.core.executor.XxlJobExecutor;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import com.xxl.job.starter.config.XxlJobAdminConfiguration;
import com.xxl.job.starter.config.XxlJobConfiguration;
import com.xxl.job.starter.config.XxlJobExecutorConfiguration;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * xxl-job属性自动装配
 */
@Configuration
@EnableConfigurationProperties({XxlJobAdminConfiguration.class, XxlJobExecutorConfiguration.class})
@ConditionalOnProperty(prefix = "xxl-job.admin", value = "addresses")
public class XxlJobAutoConfiguration implements EnvironmentAware {

    private ConfigurableEnvironment environment;

    @Bean
    @ConditionalOnProperty(prefix = "xxl-jon.executor", value = "address")
    public XxlJobExecutor xxlJobExecutor(@NonNull XxlJobAdminConfiguration adminConfiguration, @NonNull XxlJobExecutorConfiguration executorConfiguration){
        XxlJobExecutor executor = new XxlJobSpringExecutor();
        executor.setAdminAddresses(adminConfiguration.getAddresses());
        executor.setAccessToken(adminConfiguration.getAccessToken());

        if (StringUtils.hasLength(executorConfiguration.getAppName())){
            executor.setAppname(executorConfiguration.getAppName());
        } else {
            String applicationName = environment.getProperty("spring.application.name");
            if (!StringUtils.hasLength(applicationName)){
                throw new IllegalArgumentException("The name of executor is empty.");
            }
            executor.setAppname(applicationName);
        }

        executor.setAppname(appname);
        executor.setAddress(address);
        executor.setIp(ip);
        executor.setPort(port);
        executor.setAccessToken(accessToken);
        executor.setLogPath(logPath);
        executor.setLogRetentionDays(logRetentionDays);
        return executor;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = (ConfigurableEnvironment) environment;
    }
}
