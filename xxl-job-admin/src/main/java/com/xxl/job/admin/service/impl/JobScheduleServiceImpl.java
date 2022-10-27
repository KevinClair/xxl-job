package com.xxl.job.admin.service.impl;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.xxl.job.admin.common.Constants;
import com.xxl.job.admin.core.conf.XxlJobAdminConfig;
import com.xxl.job.admin.core.cron.CronExpression;
import com.xxl.job.admin.core.model.XxlJobInfo;
import com.xxl.job.admin.core.scheduler.MisfireStrategyEnum;
import com.xxl.job.admin.core.scheduler.ScheduleTypeEnum;
import com.xxl.job.admin.core.thread.JobTriggerPoolHelper;
import com.xxl.job.admin.core.trigger.TriggerTypeEnum;
import com.xxl.job.admin.dao.XxlJobInfoDao;
import com.xxl.job.admin.dao.XxlJobLockDao;
import com.xxl.job.admin.service.JobScheduleService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobScheduleServiceImpl implements JobScheduleService {

    private static final Logger logger = LoggerFactory.getLogger(JobScheduleServiceImpl.class);

    private final XxlJobLockDao jobLockDao;

    private final XxlJobInfoDao jobInfoDao;

    private final JobTriggerPoolHelper triggerPoolHelper;

    public JobScheduleServiceImpl(final XxlJobLockDao jobLockDao, final XxlJobInfoDao jobInfoDao,
                                  final JobTriggerPoolHelper triggerPoolHelper) {
        this.jobLockDao = jobLockDao;
        this.jobInfoDao = jobInfoDao;
        this.triggerPoolHelper = triggerPoolHelper;
    }

    @Override
    @Transactional
    public boolean execute(Integer preReadCount, Map<Integer, List<Integer>> ringData) throws ParseException {
        // 开始分布式锁
        jobLockDao.lock();
        logger.info("获取到锁啦");
        long nowTime = System.currentTimeMillis();
        List<XxlJobInfo> scheduleList = jobInfoDao.scheduleJobQuery(nowTime + Constants.PRE_READ_MS, preReadCount);
        if (scheduleList.isEmpty()){
            return false;
        }
        // |         now-PRE_READ_MS                       now                      now+PRE_READ_MS
        // |-------A--------|----------------B--------------|-----------------C-------------|
        // scheduleList查询的数据为所有调度时间小于now+PRE_READ_MS的数据，进行遍历
        for (XxlJobInfo jobInfo : scheduleList) {
            // time-ring jump
            if (nowTime > jobInfo.getTriggerNextTime() + Constants.PRE_READ_MS) {
                // 当前任务的调度时间，在A范围内，根据调度过期策略选择忽略或者立即执行一次，并且刷新下一次的执行时间

                // 2.1、trigger-expire > 5s：pass && make next-trigger-time
                logger.warn(">>>>>>>>>>> xxl-job, schedule misfire, jobId = " + jobInfo.getId());

                // 1、misfire match
                MisfireStrategyEnum
                    misfireStrategyEnum = MisfireStrategyEnum.match(jobInfo.getMisfireStrategy(), MisfireStrategyEnum.DO_NOTHING);
                if (MisfireStrategyEnum.FIRE_ONCE_NOW == misfireStrategyEnum) {
                    // FIRE_ONCE_NOW 》 trigger
                    triggerPoolHelper.trigger(jobInfo.getId(), TriggerTypeEnum.MISFIRE, -1, null, null, null);
                    logger.debug(">>>>>>>>>>> xxl-job, schedule push trigger : jobId = " + jobInfo.getId() );
                }

                // 2、fresh next
                refreshNextValidTime(jobInfo, new Date());

            } else if (nowTime > jobInfo.getTriggerNextTime()) {
                // 当前任务的调度时间，在B范围内，直接执行，并且刷新下一次的执行时间
                // 2.2、trigger-expire < 5s：direct-trigger && make next-trigger-time
                // 1、trigger
                triggerPoolHelper.trigger(jobInfo.getId(), TriggerTypeEnum.CRON, -1, null, null, null);
                logger.debug(">>>>>>>>>>> xxl-job, schedule push trigger : jobId = " + jobInfo.getId() );

                // 2、fresh next
                refreshNextValidTime(jobInfo, new Date());

                // next-trigger-time in 5s, pre-read again
                // 如果此时当前调度任务的下一次执行时间依然是小于nowTime + PRE_READ_MS
                // todo
                if (jobInfo.getTriggerStatus()==1 && nowTime + Constants.PRE_READ_MS > jobInfo.getTriggerNextTime()) {

                    // 1、make ring second
                    int ringSecond = (int)((jobInfo.getTriggerNextTime()/1000)%60);

                    // 2、push time ring
                    pushTimeRing(ringSecond, jobInfo.getId(), ringData);

                    // 3、fresh next
                    refreshNextValidTime(jobInfo, new Date(jobInfo.getTriggerNextTime()));

                }
            } else {
                // 当前任务的调度时间在C范围内
                // 2.3、trigger-pre-read：time-ring trigger && make next-trigger-time

                // 1、make ring second
                int ringSecond = (int)((jobInfo.getTriggerNextTime()/1000)%60);

                // 2、push time ring
                pushTimeRing(ringSecond, jobInfo.getId(), ringData);

                // 3、fresh next
                refreshNextValidTime(jobInfo, new Date(jobInfo.getTriggerNextTime()));

            }
        }

        // 3、update trigger info
        for (XxlJobInfo jobInfo: scheduleList) {
            jobInfoDao.scheduleUpdate(jobInfo);
        }
        return true;
    }

    private void refreshNextValidTime(XxlJobInfo jobInfo, Date fromTime) throws ParseException {
        Date nextValidTime = generateNextValidTime(jobInfo, fromTime);
        if (nextValidTime != null) {
            jobInfo.setTriggerLastTime(jobInfo.getTriggerNextTime());
            jobInfo.setTriggerNextTime(nextValidTime.getTime());
        } else {
            jobInfo.setTriggerStatus(0);
            jobInfo.setTriggerLastTime(0);
            jobInfo.setTriggerNextTime(0);
            logger.warn(">>>>>>>>>>> xxl-job, refreshNextValidTime fail for job: jobId={}, scheduleType={}, scheduleConf={}",
                jobInfo.getId(), jobInfo.getScheduleType(), jobInfo.getScheduleConf());
        }
    }

    private static Date generateNextValidTime(XxlJobInfo jobInfo, Date fromTime) throws ParseException {
        ScheduleTypeEnum scheduleTypeEnum = ScheduleTypeEnum.match(jobInfo.getScheduleType(), null);
        if (ScheduleTypeEnum.CRON == scheduleTypeEnum) {
            Date nextValidTime = new CronExpression(jobInfo.getScheduleConf()).getNextValidTimeAfter(fromTime);
            return nextValidTime;
        } else if (ScheduleTypeEnum.FIX_RATE == scheduleTypeEnum /*|| ScheduleTypeEnum.FIX_DELAY == scheduleTypeEnum*/) {
            return new Date(fromTime.getTime() + Integer.valueOf(jobInfo.getScheduleConf())*1000 );
        }
        return null;
    }

    private void pushTimeRing(int ringSecond, int jobId, Map<Integer, List<Integer>> ringData){
        // push async ring
        List<Integer> ringItemData = ringData.computeIfAbsent(ringSecond, k -> new ArrayList<>());
        ringItemData.add(jobId);

        logger.debug(">>>>>>>>>>> xxl-job, schedule push time-ring : " + ringSecond + " = " + Arrays.asList(ringItemData) );
    }
}
