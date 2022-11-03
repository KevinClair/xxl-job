package com.xxl.job.core.executor.impl;

import com.xxl.job.core.executor.XxlJobExecutor;
import com.xxl.job.core.executor.config.XxlJobConfiguration;
import com.xxl.job.core.glue.GlueFactory;
import com.xxl.job.core.handler.JobHandlerRepository;
import com.xxl.job.core.handler.JobThreadRepository;
import com.xxl.job.core.log.XxlJobFileAppender;
import com.xxl.job.core.thread.JobLogFileCleanThread;
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

    private static final Logger logger = LoggerFactory.getLogger(XxlJobSpringExecutor.class);

    private final XxlJobConfiguration configuration;

    private final TriggerCallbackThread triggerCallbackThread;

    public XxlJobSpringExecutor(final XxlJobConfiguration configuration, final TriggerCallbackThread triggerCallbackThread) {
        this.configuration = configuration;
        this.triggerCallbackThread = triggerCallbackThread;
    }

    // start
    @Override
    public void afterPropertiesSet() {

        // refresh GlueFactory
        GlueFactory.refreshInstance(1);

        // super start
        // init logpath
        XxlJobFileAppender.initLogPath(configuration.getLogPath());

        // init JobLogFileCleanThread
        JobLogFileCleanThread.getInstance().start(configuration.getLogRetentionDays());

        // init TriggerCallbackThread
        triggerCallbackThread.start();
    }

    // destroy
    @Override
    public void destroy() {
        JobThreadRepository.destroy();
        // destroy JobLogFileCleanThread
        JobLogFileCleanThread.getInstance().toStop();
    }
}
