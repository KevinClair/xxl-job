package com.xxl.job.executor.sample.frameless.config;

import com.xxl.job.core.executor.AdminBizClientManager;
import com.xxl.job.core.executor.config.XxlJobConfiguration;
import com.xxl.job.core.thread.TriggerCallbackThread;
import com.xxl.job.executor.sample.frameless.jobhandler.SampleXxlJob;
import com.xxl.job.core.executor.impl.XxlJobSimpleExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Properties;

/**
 * @author xuxueli 2018-10-31 19:05:43
 */
public class FrameLessXxlJobConfig {
    private static Logger logger = LoggerFactory.getLogger(FrameLessXxlJobConfig.class);


    private static FrameLessXxlJobConfig instance = new FrameLessXxlJobConfig();
    public static FrameLessXxlJobConfig getInstance() {
        return instance;
    }


    private XxlJobSimpleExecutor xxlJobExecutor = null;

    /**
     * init
     */
    public void initXxlJobExecutor() {

        // load executor prop
        Properties xxlJobProp = loadProperties("xxl-job-executor.properties");

        // init executor
        XxlJobConfiguration configuration = new XxlJobConfiguration();
        configuration.setAddress(xxlJobProp.getProperty("xxl.job.admin.addresses"));
        configuration.setAccessToken(xxlJobProp.getProperty("xxl.job.accessToken"));
        configuration.setAppName(xxlJobProp.getProperty("xxl.job.executor.appname"));
        configuration.setAddress(xxlJobProp.getProperty("xxl.job.executor.address"));
        configuration.setIp(xxlJobProp.getProperty("xxl.job.executor.ip"));
        configuration.setPort(Integer.valueOf(xxlJobProp.getProperty("xxl.job.executor.port")));
        configuration.setLogPath(xxlJobProp.getProperty("xxl.job.executor.logpath"));
        configuration.setLogRetentionDays(Integer.valueOf(xxlJobProp.getProperty("xxl.job.executor.logretentiondays")));

        AdminBizClientManager bizClientManager = new AdminBizClientManager(configuration);
        TriggerCallbackThread callbackThread = new TriggerCallbackThread(bizClientManager);
        xxlJobExecutor = new XxlJobSimpleExecutor(configuration, callbackThread);

        // registry job bean
        xxlJobExecutor.setXxlJobBeanList(Arrays.asList(new SampleXxlJob()));

        // start executor
        try {
            xxlJobExecutor.start();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * destroy
     */
    public void destroyXxlJobExecutor() {
        if (xxlJobExecutor != null) {
            xxlJobExecutor.destroy();
        }
    }


    public static Properties loadProperties(String propertyFileName) {
        InputStreamReader in = null;
        try {
            ClassLoader loder = Thread.currentThread().getContextClassLoader();

            in = new InputStreamReader(loder.getResourceAsStream(propertyFileName), "UTF-8");;
            if (in != null) {
                Properties prop = new Properties();
                prop.load(in);
                return prop;
            }
        } catch (IOException e) {
            logger.error("load {} error!", propertyFileName);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    logger.error("close {} error!", propertyFileName);
                }
            }
        }
        return null;
    }

}
