package com.xxl.job.core.executor.impl;

import com.xxl.job.core.executor.config.XxlJobConfiguration;
import com.xxl.job.core.glue.GlueFactory;
import com.xxl.job.core.handler.JobThreadRepository;
import com.xxl.job.core.log.XxlJobFileAppender;
import com.xxl.job.core.thread.JobLogFileCleanHandler;
import com.xxl.job.core.thread.TriggerCallbackThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;


/**
 * xxl-job executor (for spring)
 *
 * @author xuxueli 2018-11-01 09:24:52
 */
public class XxlJobSpringExecutor implements InitializingBean, DisposableBean {

    private final XxlJobConfiguration configuration;

    private final JobLogFileCleanHandler jobLogFileCleanHandler;

    public XxlJobSpringExecutor(final XxlJobConfiguration configuration) {
        this.configuration = configuration;
        this.jobLogFileCleanHandler = new JobLogFileCleanHandler(configuration.getLogRetentionDays(), configuration.getLogPath());
    }

    // start
    @Override
    public void afterPropertiesSet() {

        // refresh GlueFactory
        GlueFactory.refreshInstance(1);

        // init logpath
        XxlJobFileAppender.initLogPath(configuration.getLogPath());
    }

    // destroy
    @Override
    public void destroy() {
        JobThreadRepository.destroy();
        // destroy JobLogFileCleanThread
        jobLogFileCleanHandler.toStop();
    }
}
