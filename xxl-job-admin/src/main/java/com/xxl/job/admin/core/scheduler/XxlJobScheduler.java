package com.xxl.job.admin.core.scheduler;

import com.xxl.job.admin.core.conf.XxlJobAdminConfig;
import com.xxl.job.admin.core.thread.*;
import com.xxl.job.common.utils.I18nUtil;
import com.xxl.job.core.enums.ExecutorBlockStrategyEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * @author xuxueli 2018-10-28 00:18:17
 */
@Component
public class XxlJobScheduler implements InitializingBean, DisposableBean {
    private static final Logger logger = LoggerFactory.getLogger(XxlJobScheduler.class);

    private final XxlJobAdminConfig jobAdminConfig;

    private final JobRegistryHelper registryHelper;

    private final JobFailMonitorHelper failMonitorHelper;

    private final JobCompleteHelper completeHelper;

    private final JobLogReportHelper logReportHelper;

    private final JobScheduleHelper scheduleHelper;

    private final JobTriggerPoolHelper triggerPoolHelper;

    public XxlJobScheduler(XxlJobAdminConfig jobAdminConfig, JobRegistryHelper registryHelper, JobFailMonitorHelper failMonitorHelper, JobCompleteHelper completeHelper, JobLogReportHelper logReportHelper, JobScheduleHelper scheduleHelper, JobTriggerPoolHelper triggerPoolHelper) {
        this.jobAdminConfig = jobAdminConfig;
        this.registryHelper = registryHelper;
        this.failMonitorHelper = failMonitorHelper;
        this.completeHelper = completeHelper;
        this.logReportHelper = logReportHelper;
        this.scheduleHelper = scheduleHelper;
        this.triggerPoolHelper = triggerPoolHelper;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // init i18n
        initI18n();

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
        triggerPoolHelper.stop();

    }

    // ---------------------- I18n ----------------------

    private void initI18n() {
        for (ExecutorBlockStrategyEnum item : ExecutorBlockStrategyEnum.values()) {
            item.setTitle(I18nUtil.getString("jobconf_block_".concat(item.name())));
        }
        I18nUtil.initProperties(jobAdminConfig.getI18n());
    }
}
