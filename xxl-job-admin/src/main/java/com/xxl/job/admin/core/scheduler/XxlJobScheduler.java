package com.xxl.job.admin.core.scheduler;

import com.xxl.job.admin.core.conf.XxlJobAdminConfig;
import com.xxl.job.admin.core.thread.*;
import com.xxl.job.admin.core.util.I18nUtil;
import com.xxl.job.core.biz.ExecutorBiz;
import com.xxl.job.core.biz.client.ExecutorBizClient;
import com.xxl.job.core.enums.ExecutorBlockStrategyEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Resource;

/**
 * @author xuxueli 2018-10-28 00:18:17
 */
@Component
public class XxlJobScheduler implements InitializingBean, DisposableBean {
    private static final Logger logger = LoggerFactory.getLogger(XxlJobScheduler.class);

    @Resource
    private XxlJobAdminConfig jobAdminConfig;

    @Resource
    private JobRegistryHelper registryHelper;

    @Resource
    private JobFailMonitorHelper failMonitorHelper;

    @Resource
    private JobCompleteHelper completeHelper;

    @Resource
    private JobLogReportHelper logReportHelper;

    @Resource
    private JobScheduleHelper scheduleHelper;

    @Override
    public void afterPropertiesSet() throws Exception {
        // init i18n
        initI18n();

        // admin trigger pool start
        JobTriggerPoolHelper.toStart();

        // admin registry monitor run
        registryHelper.start();

        // admin fail-monitor run
        failMonitorHelper.start();

        // admin lose-monitor run ( depend on JobTriggerPoolHelper )
        completeHelper.start();

        // admin log report start
        logReportHelper.start();

        // start-schedule  ( depend on JobTriggerPoolHelper )
        scheduleHelper.start();

        logger.info(">>>>>>>>> init xxl-job admin success.");
    }

    @Override
    public void destroy() throws Exception {

        // stop-schedule
        scheduleHelper.toStop();

        // admin log report stop
        logReportHelper.toStop();

        // admin lose-monitor stop
        completeHelper.toStop();

        // admin fail-monitor stop
        failMonitorHelper.toStop();

        // admin registry stop
        registryHelper.toStop();

        // admin trigger pool stop
        JobTriggerPoolHelper.toStop();

    }

    // ---------------------- I18n ----------------------

    private void initI18n(){
        for (ExecutorBlockStrategyEnum item:ExecutorBlockStrategyEnum.values()) {
            item.setTitle(I18nUtil.getString("jobconf_block_".concat(item.name())));
        }
    }
}
