package com.xxl.job.admin.service.impl;

import com.xxl.job.admin.core.thread.JobCompleteHelper;
import com.xxl.job.admin.service.JobRemoteApiService;
import com.xxl.job.common.dto.AddXxlJobInfoDto;
import com.xxl.job.common.dto.DeleteXxlJobInfoDto;
import com.xxl.job.common.dto.SaveXxlJobInfoDto;
import com.xxl.job.common.dto.UpdateXxlJobInfoDto;
import com.xxl.job.common.model.HandleCallbackParam;
import com.xxl.job.common.model.RegistryParam;
import com.xxl.job.common.model.ReturnT;
import com.xxl.job.common.service.AdminManager;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Admin端接受到客户端(应用端)后的本地实现
 *
 * @author xuxueli 2017-07-27 21:54:20
 */
@Service
public class AdminManagerImpl implements AdminManager {

    private final JobCompleteHelper jobCompleteHelper;

    private final JobRemoteApiService remoteApiService;

    public AdminManagerImpl(JobCompleteHelper jobCompleteHelper, JobRemoteApiService remoteApiService) {
        this.jobCompleteHelper = jobCompleteHelper;
        this.remoteApiService = remoteApiService;
    }

    @Override
    public ReturnT<String> callback(List<HandleCallbackParam> callbackParamList) {
        return jobCompleteHelper.callback(callbackParamList);
    }

    @Override
    public ReturnT<String> registry(RegistryParam registryParam) {
        return ReturnT.success(remoteApiService.registry(registryParam));
    }

    @Override
    public ReturnT<String> registryRemove(RegistryParam registryParam) {
        return ReturnT.success(remoteApiService.registryRemove(registryParam));
    }

    @Override
    public ReturnT<String> addJob(AddXxlJobInfoDto request) {
        return ReturnT.success(remoteApiService.addJob(request));
    }

    @Override
    public ReturnT<String> deleteJob(DeleteXxlJobInfoDto request) {
        return ReturnT.success(remoteApiService.deleteJob(request));
    }

    @Override
    public ReturnT<String> updateJob(UpdateXxlJobInfoDto request) {
        return ReturnT.success(remoteApiService.updateJob(request));
    }

    @Override
    public ReturnT<String> saveJob(SaveXxlJobInfoDto request) {
        return ReturnT.success(remoteApiService.saveJob(request));
    }
}
