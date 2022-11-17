package com.xxl.job.starter;

import java.util.Properties;

import com.xxl.job.core.biz.ExecutorBiz;
import com.xxl.job.core.biz.impl.ExecutorBizImpl;
import com.xxl.job.core.executor.AdminBizClientManager;
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
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
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
@ConditionalOnProperty(prefix = "xxl-job.admin", value = "address")
public class XxlJobAutoConfiguration implements EnvironmentAware, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(XxlJobAutoConfiguration.class);

    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private static final String PATH = "/META-INF/maven/com.xuxueli/xxl-job-spring-boot-starter/pom.properties";

    private static final String BANNAR = "\n" +
        "██╗  ██╗██╗  ██╗██╗               ██╗ ██████╗ ██████╗ \n" +
        "╚██╗██╔╝╚██╗██╔╝██║               ██║██╔═══██╗██╔══██╗\n" +
        " ╚███╔╝  ╚███╔╝ ██║    █████╗     ██║██║   ██║██████╔╝\n" +
        " ██╔██╗  ██╔██╗ ██║    ╚════╝██   ██║██║   ██║██╔══██╗\n" +
        "██╔╝ ██╗██╔╝ ██╗███████╗     ╚█████╔╝╚██████╔╝██████╔╝\n" +
        "╚═╝  ╚═╝╚═╝  ╚═╝╚══════╝      ╚════╝  ╚═════╝ ╚═════╝ \n";

    private ConfigurableEnvironment environment;

    @Override
    public void afterPropertiesSet() throws Exception{
        String version = null;
        try {
            Properties properties = new Properties();
            properties.load(XxlJobAutoConfiguration.class.getResourceAsStream(PATH));
            version = properties.getProperty("version");
        } catch (Exception e) {
        }
        StringBuilder bannerTextBuilder = new StringBuilder();
        bannerTextBuilder.append(LINE_SEPARATOR).append(BANNAR).append(" :: xxl-job ::         (v").append(version).append(")").append(LINE_SEPARATOR);
        logger.info(bannerTextBuilder.toString());
    }

    @Bean
    public XxlJobConfiguration xxlJobConfiguration(@NonNull XxlJobAdminConfiguration adminConfiguration, @NonNull XxlJobExecutorConfiguration executorConfiguration) {
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
        configuration.setExecutorAddress("http://"+configuration.getIp()+":"+configuration.getPort());
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
    public XxlJobSpringExecutor xxlJobSpringExecutor(XxlJobConfiguration configuration){
        return new XxlJobSpringExecutor(configuration);
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
