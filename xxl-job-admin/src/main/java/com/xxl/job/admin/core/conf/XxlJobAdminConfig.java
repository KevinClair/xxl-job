package com.xxl.job.admin.core.conf;

import com.xxl.job.admin.core.alarm.JobAlarmer;
import com.xxl.job.admin.core.scheduler.XxlJobScheduler;
import com.xxl.job.admin.dao.*;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.Arrays;

/**
 * xxl-job config
 *
 * @author xuxueli 2017-04-28
 */
@Configuration
public class XxlJobAdminConfig {

    // conf
    @Value("${xxl.job.i18n}")
    private String i18n;

    @Value("${xxl.job.accessToken}")
    private String accessToken;

    @Value("${spring.mail.from}")
    private String emailFrom;

    @Value("${xxl.job.triggerpool.fast.max}")
    private int triggerPoolFastMax;

    @Value("${xxl.job.triggerpool.slow.max}")
    private int triggerPoolSlowMax;

    @Value("${xxl.job.logretentiondays}")
    private int logretentiondays;

    public String getI18n() {
        if (!Arrays.asList("zh_CN", "zh_TC", "en").contains(i18n)) {
            return "zh_CN";
        }
        return i18n;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getEmailFrom() {
        return emailFrom;
    }

    public int getTriggerPoolFastMax() {
        if (triggerPoolFastMax < 200) {
            return 200;
        }
        return triggerPoolFastMax;
    }

    public int getTriggerPoolSlowMax() {
        if (triggerPoolSlowMax < 100) {
            return 100;
        }
        return triggerPoolSlowMax;
    }

    public int getLogretentiondays() {
        if (logretentiondays < 7) {
            return -1;  // Limit greater than or equal to 7, otherwise close
        }
        return logretentiondays;
    }

    /**
     * Sets the i18n.
     *
     * @param i18n i18n
     */
    public void setI18n(final String i18n) {
        this.i18n = i18n;
    }

    /**
     * Sets the accessToken.
     *
     * @param accessToken accessToken
     */
    public void setAccessToken(final String accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * Sets the emailFrom.
     *
     * @param emailFrom emailFrom
     */
    public void setEmailFrom(final String emailFrom) {
        this.emailFrom = emailFrom;
    }

    /**
     * Sets the triggerPoolFastMax.
     *
     * @param triggerPoolFastMax triggerPoolFastMax
     */
    public void setTriggerPoolFastMax(final int triggerPoolFastMax) {
        this.triggerPoolFastMax = triggerPoolFastMax;
    }

    /**
     * Sets the triggerPoolSlowMax.
     *
     * @param triggerPoolSlowMax triggerPoolSlowMax
     */
    public void setTriggerPoolSlowMax(final int triggerPoolSlowMax) {
        this.triggerPoolSlowMax = triggerPoolSlowMax;
    }

    /**
     * Sets the logretentiondays.
     *
     * @param logretentiondays logretentiondays
     */
    public void setLogretentiondays(final int logretentiondays) {
        this.logretentiondays = logretentiondays;
    }
}
