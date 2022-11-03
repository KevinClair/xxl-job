package com.xxl.job.core.executor.impl;

import com.xxl.job.core.executor.XxlJobExecutor;
import com.xxl.job.core.executor.config.XxlJobConfiguration;
import com.xxl.job.core.glue.GlueFactory;
import com.xxl.job.core.handler.JobHandlerRepository;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.core.thread.TriggerCallbackThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;
import java.util.Map;


/**
 * xxl-job executor (for spring)
 *
 * @author xuxueli 2018-11-01 09:24:52
 */
public class XxlJobSpringExecutor extends XxlJobExecutor implements InitializingBean, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(XxlJobSpringExecutor.class);

    private final JobHandlerRepository jobHandlerRepository;

    public XxlJobSpringExecutor(final XxlJobConfiguration configuration, final TriggerCallbackThread triggerCallbackThread,
                                final JobHandlerRepository jobHandlerRepository) {
        super(configuration, triggerCallbackThread, jobHandlerRepository);
        this.jobHandlerRepository = jobHandlerRepository;
    }

    // start
    @Override
    public void afterPropertiesSet() {

        // init JobHandler Repository (for method)
        jobHandlerRepository.initJobHandlerMethodRepository();

        // refresh GlueFactory
        GlueFactory.refreshInstance(1);

        // super start
        try {
            super.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // destroy
    @Override
    public void destroy() {
        super.destroy();
    }
}
