package com.xxl.job.starter;

import com.xxl.job.core.biz.ExecutorBiz;
import com.xxl.job.core.biz.impl.ExecutorBizImpl;
import com.xxl.job.core.executor.AdminBizClientManager;
import com.xxl.job.core.executor.XxlJobExecutor;
import com.xxl.job.core.executor.config.XxlJobConfiguration;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import com.xxl.job.core.handler.JobHandlerRepository;
import com.xxl.job.core.server.EmbedServer;
import com.xxl.job.core.thread.ExecutorRegistryThread;
import com.xxl.job.core.thread.TriggerCallbackThread;
import com.xxl.job.core.util.IpUtil;
import com.xxl.job.core.util.NetUtil;
import com.xxl.job.starter.config.XxlJobAdminConfiguration;
import com.xxl.job.starter.config.XxlJobExecutorConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(XxlJobAutoConfiguration.class);

    private ConfigurableEnvironment environment;

    @Bean
    @ConditionalOnProperty(prefix = "xxl-job.executor", value = "address")
    public XxlJobConfiguration xxlJobExecutor(@NonNull XxlJobAdminConfiguration adminConfiguration, @NonNull XxlJobExecutorConfiguration executorConfiguration) {
        XxlJobConfiguration configuration = new XxlJobConfiguration();
        configuration.setAddress(adminConfiguration.getAddress());
        if (!StringUtils.hasLength(adminConfiguration.getAccessToken())){
            logger.warn("xxl-job accessToken is empty. To ensure system security, please set the accessToken.");
        }else {
            configuration.setAccessToken(adminConfiguration.getAccessToken());
        }
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

    @Bean
    public JobHandlerRepository jobHandlerRepository(){
        return new JobHandlerRepository();
    }

    @Bean
    public ExecutorBiz executorBiz(JobHandlerRepository jobHandlerRepository){
        return new ExecutorBizImpl(jobHandlerRepository);
    }

    @Bean
    public AdminBizClientManager adminBizClientManager(XxlJobConfiguration configuration){
        return new AdminBizClientManager(configuration);
    }

    @Bean
    public TriggerCallbackThread triggerCallbackThread(AdminBizClientManager bizClientManager){
        return new TriggerCallbackThread(bizClientManager);
    }

    @Bean
    public XxlJobExecutor xxlJobExecutor(XxlJobConfiguration configuration, TriggerCallbackThread triggerCallbackThread, JobHandlerRepository jobHandlerRepository){
        return new XxlJobSpringExecutor(configuration, triggerCallbackThread, jobHandlerRepository);
    }

    @Bean
    public ExecutorRegistryThread executorRegistryThread(AdminBizClientManager bizClientManager, XxlJobConfiguration configuration){
        return new ExecutorRegistryThread(bizClientManager, configuration);
    }

    @Bean
    public EmbedServer embedServer(ExecutorBiz executorBiz, XxlJobConfiguration configuration, ExecutorRegistryThread executorRegistryThread){
        return new EmbedServer(executorBiz, configuration, executorRegistryThread);
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = (ConfigurableEnvironment) environment;
    }
}
