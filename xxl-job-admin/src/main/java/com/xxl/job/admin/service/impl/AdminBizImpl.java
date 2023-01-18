package com.xxl.job.admin.service.impl;

import com.xxl.job.admin.core.thread.JobCompleteHelper;
import com.xxl.job.admin.core.thread.JobRegistryHelper;
import com.xxl.job.core.biz.AdminBiz;
import com.xxl.job.core.biz.model.HandleCallbackParam;
import com.xxl.job.core.biz.model.RegistryParam;
import com.xxl.job.core.biz.model.ReturnT;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author xuxueli 2017-07-27 21:54:20
 */
@Service
public class AdminBizImpl implements AdminBiz {

    private final JobCompleteHelper jobCompleteHelper;

    private final JobRegistryHelper jobRegistryHelper;

    public AdminBizImpl(JobCompleteHelper jobCompleteHelper, JobRegistryHelper jobRegistryHelper) {
        this.jobCompleteHelper = jobCompleteHelper;
        this.jobRegistryHelper = jobRegistryHelper;
    }

    @Override
    public ReturnT<String> callback(List<HandleCallbackParam> callbackParamList) {
        return jobCompleteHelper.callback(callbackParamList);
    }

    @Override
    public ReturnT<String> registry(RegistryParam registryParam) {
        return jobRegistryHelper.registry(registryParam);
    }

    @Override
    public ReturnT<String> registryRemove(RegistryParam registryParam) {
        return jobRegistryHelper.registryRemove(registryParam);
    }

    @Override
    public ReturnT<String> addJob() {
        return null;
    }

    @Override
    public ReturnT<String> deleteJob() {
        return null;
    }
}
