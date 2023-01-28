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
import com.xxl.job.common.dto.SaveXxlJobInfoDto;
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
        xxlJobInfo.setScheduleType(request.getScheduleType().name());
        xxlJobInfo.setScheduleConf(request.getScheduleConf());
        xxlJobInfo.setMisfireStrategy(request.getMisfireStrategy().name());
        xxlJobInfo.setExecutorRouteStrategy(request.getExecutorRouteStrategy().name());
        xxlJobInfo.setExecutorHandler(request.getExecutorHandler());
        xxlJobInfo.setExecutorParam(request.getExecutorParam());
        xxlJobInfo.setExecutorBlockStrategy(request.getExecutorBlockStrategy().name());
        xxlJobInfo.setExecutorTimeout(request.getExecutorTimeout());
        xxlJobInfo.setExecutorFailRetryCount(request.getExecutorFailRetryCount());
        xxlJobInfo.setGlueType(GlueTypeEnum.BEAN.getDesc());
        xxlJobInfo.setGlueUpdatetime(now);
        if (!CollectionUtils.isEmpty(childJobValidList)) {
            xxlJobInfo.setChildJobId(childJobValidList.stream().collect(Collectors.joining(",")));
        }
        xxlJobInfo.setTriggerStatus(1);
        xxlJobInfoDao.save(xxlJobInfo);
        return "success";
    }

    @Override
    public String updateJob(UpdateXxlJobInfoDto request) {
        // 校验jobId合法性
        XxlJobInfo jobInfo = xxlJobInfoDao.loadById(request.getJobId());
        if (Objects.isNull(jobInfo)) {
            throw new XxlJobException("Invalid job id.");
        }
        // 校验corn表达式
        if (Objects.nonNull(request.getScheduleType()) && request.getScheduleType().equals(ScheduleTypeEnum.CRON) && !CronExpression.isValidExpression(request.getScheduleConf())) {
            throw new XxlJobException("invalid corn");
        }
        jobInfo.setScheduleType(request.getScheduleType().getTitle());
        jobInfo.setScheduleConf(request.getScheduleConf());
        if (Objects.nonNull(request.getTriggerStatus())) {
            jobInfo.setTriggerStatus(request.getTriggerStatus());
        }
        Date now = new Date();
        jobInfo.setUpdateTime(now);
        jobInfo.setGlueUpdatetime(now);
        // todo 考虑是否可以提供修改下次执行时间选项
        xxlJobInfoDao.update(jobInfo);
        return "success";
    }

    @Override
    public String deleteJob(DeleteXxlJobInfoDto request) {
        // 如果jobId不为空以jobId为准删除数据
        if (Objects.nonNull(request.getJobId())) {
            int delete = xxlJobInfoDao.delete(request.getJobId());
            if (delete > 0) {
                return "success";
            } else {
                return "invalid job id, delete error.";
            }
        }
        int delete = xxlJobInfoDao.deleteByJobHandler(request.getJobHandlerName());
        if (delete > 0) {
            return "success";
        } else {
            return "invalid job handler, delete error.";
        }
    }

    @Override
    public String saveJob(SaveXxlJobInfoDto request) {
        // 根据jobDesc查询，不存在就新增
        XxlJobInfo xxlJobInfo = xxlJobInfoDao.findByJobDesc(request.getJobDesc());
        if (Objects.isNull(xxlJobInfo)) {
            AddXxlJobInfoDto addXxlJobInfoDto = new AddXxlJobInfoDto();
            addXxlJobInfoDto.setAppName(request.getAppName());
            addXxlJobInfoDto.setJobDesc(request.getJobDesc());
            addXxlJobInfoDto.setAuthor(request.getAuthor());
            addXxlJobInfoDto.setAlarmEmail(request.getAlarmEmail());
            addXxlJobInfoDto.setScheduleType(request.getScheduleType());
            addXxlJobInfoDto.setScheduleConf(request.getScheduleConf());
            addXxlJobInfoDto.setExecutorHandler(request.getExecutorHandler());
            addXxlJobInfoDto.setExecutorParam(request.getExecutorParam());
            addXxlJobInfoDto.setExecutorRouteStrategy(request.getExecutorRouteStrategy());
            addXxlJobInfoDto.setChildJobId(request.getChildJobId());
            addXxlJobInfoDto.setMisfireStrategy(request.getMisfireStrategy());
            addXxlJobInfoDto.setExecutorBlockStrategy(request.getExecutorBlockStrategy());
            addXxlJobInfoDto.setExecutorTimeout(request.getExecutorTimeout());
            addXxlJobInfoDto.setExecutorFailRetryCount(request.getExecutorFailRetryCount());
            return this.addJob(addXxlJobInfoDto);
        }
        // 更新时，如果covered为true就更新
        if (request.getCovered()) {
            // 校验corn表达式
            if (Objects.nonNull(request.getScheduleType()) && request.getScheduleType().equals(ScheduleTypeEnum.CRON) && !CronExpression.isValidExpression(request.getScheduleConf())) {
                throw new XxlJobException("invalid corn");
            }
            xxlJobInfo.setScheduleType(request.getScheduleType().getTitle());
            xxlJobInfo.setScheduleConf(request.getScheduleConf());
            Date now = new Date();
            xxlJobInfo.setUpdateTime(now);
            xxlJobInfo.setGlueUpdatetime(now);
            // todo 考虑是否可以提供修改下次执行时间选项
            xxlJobInfoDao.update(xxlJobInfo);
            return "success";
        }
        return "success";
    }
}
