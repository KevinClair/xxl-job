package com.xxl.job.admin.core.route.strategy;

import com.xxl.job.admin.core.route.AbstractExecutorRouter;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.biz.model.TriggerParam;

import java.util.List;

/**
 * Created by xuxueli on 17/3/10.
 */
public class ExecutorRouteFirst extends AbstractExecutorRouter {

    @Override
    public ReturnT<String> routeAddress(TriggerParam triggerParam, List<String> addressList){
        return new ReturnT<String>(addressList.get(0));
    }

}
