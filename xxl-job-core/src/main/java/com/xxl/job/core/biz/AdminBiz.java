package com.xxl.job.core.biz;

import com.xxl.job.common.dto.AddXxlJobInfoDto;
import com.xxl.job.common.dto.DeleteXxlJobInfoDto;
import com.xxl.job.common.dto.UpdateXxlJobInfoDto;
import com.xxl.job.core.biz.model.HandleCallbackParam;
import com.xxl.job.core.biz.model.RegistryParam;
import com.xxl.job.core.biz.model.ReturnT;

import java.util.List;

/**
 * TODO 考虑是否将AdminBiz放在Common下面
 * @author xuxueli 2017-07-27 21:52:49
 */
public interface AdminBiz {


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
     * TODO 增加实现
     *
     * @param request {@link AddXxlJobInfoDto}
     * @return
     */
    ReturnT<String> addJob(AddXxlJobInfoDto request);

    /**
     * 删除job
     *
     * @param request {@link DeleteXxlJobInfoDto}
     *                TODO 增加实现
     * @return
     */
    ReturnT<String> deleteJob(DeleteXxlJobInfoDto request);

    /**
     * 修改Job
     * TODO 增加实现
     *
     * @param request {@link UpdateXxlJobInfoDto}
     * @return
     */
    ReturnT<String> updateJob(UpdateXxlJobInfoDto request);
}
