package com.xxl.job.core.biz.client;

import com.xxl.job.common.constant.Constants;
import com.xxl.job.common.dto.AddXxlJobInfoDto;
import com.xxl.job.common.dto.DeleteXxlJobInfoDto;
import com.xxl.job.common.dto.UpdateXxlJobInfoDto;
import com.xxl.job.core.biz.AdminBiz;
import com.xxl.job.core.biz.model.HandleCallbackParam;
import com.xxl.job.core.biz.model.RegistryParam;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.util.XxlJobRemotingUtil;

import java.util.List;

/**
 * admin api test
 *
 * @author xuxueli 2017-07-28 22:14:52
 */
public class AdminBizClient implements AdminBiz {

    private String addressUrl ;
    private String accessToken;

    private static final String ADMIN_CALLBACK = "/api/callback";
    private static final String ADMIN_REGISTRY = "/api/registry";
    private static final String ADMIN_REGISTRY_REMOVE = "/api/registryRemove";

    private static final String ADMIN_ADD_JOB = "/api/addJob";
    private static final String ADMIN_DELETE_JOB = "/api/deleteJob";


    public AdminBizClient(String addressUrl, String accessToken) {
        this.addressUrl = addressUrl;
        this.accessToken = accessToken;
    }

    @Override
    public ReturnT<String> callback(List<HandleCallbackParam> callbackParamList) {
        return XxlJobRemotingUtil.postBody(addressUrl+ADMIN_CALLBACK, accessToken, Constants.ADMIN_REQUEST_TIME_OUT, callbackParamList, String.class);
    }

    @Override
    public ReturnT<String> registry(RegistryParam registryParam) {
        return XxlJobRemotingUtil.postBody(addressUrl + ADMIN_REGISTRY, accessToken, Constants.ADMIN_REQUEST_TIME_OUT, registryParam, String.class);
    }

    @Override
    public ReturnT<String> registryRemove(RegistryParam registryParam) {
        return XxlJobRemotingUtil.postBody(addressUrl + ADMIN_REGISTRY_REMOVE, accessToken, Constants.ADMIN_REQUEST_TIME_OUT, registryParam, String.class);
    }

    @Override
    public ReturnT<String> addJob(AddXxlJobInfoDto request) {
        return null;
    }

    @Override
    public ReturnT<String> deleteJob(DeleteXxlJobInfoDto request) {
        return null;
    }

    @Override
    public ReturnT<String> updateJob(UpdateXxlJobInfoDto request) {
        return null;
    }
}
