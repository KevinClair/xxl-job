package com.xxl.job.admin.core.route;

import com.xxl.job.common.model.ReturnT;
import com.xxl.job.common.model.TriggerParam;

import java.util.List;

/**
 * 执行器路由
 */
public interface ExecutorRouter {

    /**
     * 路由规则
     *
     * @param triggerParam   执行器参数
     * @param addressList    服务端地址
     * @return
     */
    ReturnT<String> route(TriggerParam triggerParam, List<String> addressList);
}
