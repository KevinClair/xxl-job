package com.xxl.job.common.service;

import com.xxl.job.common.dto.AddXxlJobInfoDto;
import com.xxl.job.common.dto.DeleteXxlJobInfoDto;
import com.xxl.job.common.dto.UpdateXxlJobInfoDto;
import com.xxl.job.common.model.HandleCallbackParam;
import com.xxl.job.common.model.RegistryParam;
import com.xxl.job.common.model.ReturnT;

import java.util.List;

/**
 * Admin的开放接口管理
 *
 * @author xuxueli 2017-07-27 21:52:49
 */
public interface AdminManager {


    // ---------------------- callback ----------------------

    /**
     * callback
     *
     * @param callbackParamList
     * @return
     */
    ReturnT<String> callback(List<HandleCallbackParam> callbackParamList);


    // ---------------------- registry ----------------------

    /**
     * registry
     *
     * @param registryParam
     * @return
     */
    ReturnT<String> registry(RegistryParam registryParam);

    /**
     * registry remove
     *
     * @param registryParam
     * @return
     */
    ReturnT<String> registryRemove(RegistryParam registryParam);


    // ---------------------- biz (custome) ----------------------
    // group、job ... manage

    /**
     * 添加Job
     *
     * @param request {@link AddXxlJobInfoDto}
     * @return
     */
    ReturnT<String> addJob(AddXxlJobInfoDto request);

    /**
     * 删除job
     *
     * @param request {@link DeleteXxlJobInfoDto}
     * @return
     */
    ReturnT<String> deleteJob(DeleteXxlJobInfoDto request);

    /**
     * 修改Job
     *
     * @param request {@link UpdateXxlJobInfoDto}
     * @return
     */
    ReturnT<String> updateJob(UpdateXxlJobInfoDto request);
}
