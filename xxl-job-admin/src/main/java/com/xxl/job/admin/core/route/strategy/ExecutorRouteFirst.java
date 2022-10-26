package com.xxl.job.admin.core.route.strategy;

import com.xxl.job.admin.core.route.AbstractExecutorRouter;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.biz.model.TriggerParam;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by xuxueli on 17/3/10.
 */
@Component("executorRouteFirst")
public class ExecutorRouteFirst extends AbstractExecutorRouter {

    @Override
    public ReturnT<String> routeAddress(TriggerParam triggerParam, List<String> addressList){
        return new ReturnT<String>(addressList.get(0));
    }

}
