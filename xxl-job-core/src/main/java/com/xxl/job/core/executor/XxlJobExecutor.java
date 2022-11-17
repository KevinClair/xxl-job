package com.xxl.job.core.executor;

import com.xxl.job.core.executor.config.XxlJobConfiguration;
import com.xxl.job.core.handler.JobThreadRepository;
import com.xxl.job.core.log.XxlJobFileAppender;
import com.xxl.job.core.thread.JobLogFileCleanHandler;
import com.xxl.job.core.thread.TriggerCallbackThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by xuxueli on 2016/3/2 21:14.
 */
public class XxlJobExecutor  {
    private static final Logger logger = LoggerFactory.getLogger(XxlJobExecutor.class);

    private final XxlJobConfiguration configuration;

    private final JobLogFileCleanHandler logFileCleanHandler;

    public XxlJobExecutor(final XxlJobConfiguration configuration) {
        this.configuration = configuration;
        this.logFileCleanHandler = new JobLogFileCleanHandler(configuration.getLogRetentionDays(), configuration.getLogPath());
    }

    // ---------------------- start + stop ----------------------
    public void start() throws Exception {
        // init logpath
        XxlJobFileAppender.initLogPath(configuration.getLogPath());
    }

    public void destroy(){
        JobThreadRepository.destroy();
        // destroy JobLogFileCleanThread
        logFileCleanHandler.toStop();
    }
}
