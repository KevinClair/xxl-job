package com.xxl.job.admin.service.impl;

import com.xxl.job.admin.core.cron.CronExpression;
import com.xxl.job.admin.core.exception.XxlJobException;
import com.xxl.job.admin.core.model.XxlJobGroup;
import com.xxl.job.admin.core.model.XxlJobInfo;
import com.xxl.job.admin.dao.XxlJobGroupDao;
import com.xxl.job.admin.dao.XxlJobInfoDao;
import com.xxl.job.admin.service.JobRemoteApiService;
import com.xxl.job.common.dto.AddXxlJobInfoDto;
import com.xxl.job.common.dto.DeleteXxlJobInfoDto;
import com.xxl.job.common.dto.UpdateXxlJobInfoDto;
import com.xxl.job.common.enums.GlueTypeEnum;
import com.xxl.job.common.enums.ScheduleTypeEnum;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class JobRemoteApiServiceImpl implements JobRemoteApiService {

    private final XxlJobInfoDao xxlJobInfoDao;

    private final XxlJobGroupDao xxlJobGroupDao;

    public JobRemoteApiServiceImpl(XxlJobInfoDao xxlJobInfoDao, XxlJobGroupDao xxlJobGroupDao) {
        this.xxlJobInfoDao = xxlJobInfoDao;
        this.xxlJobGroupDao = xxlJobGroupDao;
    }

    @Override
    public String addJob(AddXxlJobInfoDto request) {
        XxlJobInfo xxlJobInfo = new XxlJobInfo();

        // 校验job group合法性
        XxlJobGroup group = xxlJobGroupDao.selectByAppName(request.getAppName());
        if (Objects.isNull(group)) {
            throw new XxlJobException("invalid app name.");
        }
        // 校验corn表达式
        if (request.getScheduleType().equals(ScheduleTypeEnum.CRON) && !CronExpression.isValidExpression(request.getScheduleConf())) {
            throw new XxlJobException("invalid corn");
        }
        // 校验childJobId
        List<String> childJobValidList = new ArrayList<>();
        if (StringUtils.hasText(request.getChildJobId())) {
            List<String> childJobList = Arrays.asList(request.getChildJobId().split(",")).stream().filter(StringUtils::hasText).collect(Collectors.toList());
            for (String each : childJobList) {
                XxlJobInfo jobInfo = xxlJobInfoDao.loadById(Integer.parseInt(each));
                if (Objects.nonNull(jobInfo)) {
                    childJobValidList.add(each);
                }
            }
        }
        // 属性映射
        xxlJobInfo.setJobGroup(group.getId());
        xxlJobInfo.setJobDesc(request.getJobDesc());
        Date now = new Date();
        xxlJobInfo.setAddTime(now);
        xxlJobInfo.setUpdateTime(now);
        xxlJobInfo.setAuthor(request.getAuthor());
        xxlJobInfo.setAlarmEmail(request.getAlarmEmail());
        xxlJobInfo.setScheduleType(request.getScheduleType().getTitle());
        xxlJobInfo.setScheduleConf(request.getScheduleConf());
        xxlJobInfo.setMisfireStrategy(request.getMisfireStrategy().getTitle());
        xxlJobInfo.setExecutorRouteStrategy(request.getExecutorRouteStrategy().getTitle());
        xxlJobInfo.setExecutorHandler(request.getExecutorHandler());
        xxlJobInfo.setExecutorParam(request.getExecutorParam());
        xxlJobInfo.setExecutorBlockStrategy(request.getExecutorBlockStrategy().getTitle());
        xxlJobInfo.setExecutorTimeout(request.getExecutorTimeout());
        xxlJobInfo.setExecutorFailRetryCount(request.getExecutorFailRetryCount());
        xxlJobInfo.setGlueType(GlueTypeEnum.BEAN.getDesc());
        if (!CollectionUtils.isEmpty(childJobValidList)) {
            xxlJobInfo.setChildJobId(childJobValidList.stream().collect(Collectors.joining(",")));
        }
        xxlJobInfo.setTriggerStatus(1);
        xxlJobInfoDao.save(xxlJobInfo);
        return "success";
    }

    @Override
    public String updateJob(UpdateXxlJobInfoDto request) {
        return null;
    }

    @Override
    public String deleteJob(DeleteXxlJobInfoDto request) {
        return null;
    }
}
