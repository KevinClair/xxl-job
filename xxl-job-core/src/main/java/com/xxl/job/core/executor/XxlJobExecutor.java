package com.xxl.job.core.executor;

import com.xxl.job.core.executor.config.XxlJobConfiguration;
import com.xxl.job.core.handler.JobThreadRepository;
import com.xxl.job.core.log.XxlJobFileAppender;
import com.xxl.job.core.thread.JobLogFileCleanThread;
import com.xxl.job.core.thread.TriggerCallbackThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by xuxueli on 2016/3/2 21:14.
 */
public class XxlJobExecutor  {
    private static final Logger logger = LoggerFactory.getLogger(XxlJobExecutor.class);

    private final XxlJobConfiguration configuration;

    private final TriggerCallbackThread triggerCallbackThread;

    public XxlJobExecutor(final XxlJobConfiguration configuration, final TriggerCallbackThread triggerCallbackThread) {
        this.configuration = configuration;
        this.triggerCallbackThread = triggerCallbackThread;
    }

    // ---------------------- start + stop ----------------------
    public void start() throws Exception {
        // init logpath
        XxlJobFileAppender.initLogPath(configuration.getLogPath());

        // init JobLogFileCleanThread
        JobLogFileCleanThread.getInstance().start(configuration.getLogRetentionDays());

        // init TriggerCallbackThread
        triggerCallbackThread.start();
    }

    public void destroy(){
        JobThreadRepository.destroy();
        // destroy JobLogFileCleanThread
        JobLogFileCleanThread.getInstance().toStop();
    }
}
