package com.xxl.job.admin.core.route.strategy;

import com.xxl.job.admin.core.route.AbstractExecutorRouter;
import com.xxl.job.common.model.ReturnT;
import com.xxl.job.common.model.TriggerParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

/**
 * Created by xuxueli on 17/3/10.
 */
@Component("executorRouteRandom")
public class ExecutorRouteRandom extends AbstractExecutorRouter {

    private static Random localRandom = new Random();

    @Override
    public ReturnT<String> routeAddress(TriggerParam triggerParam, List<String> addressList) {
        String address = addressList.get(localRandom.nextInt(addressList.size()));
        return new ReturnT<String>(address);
    }

}
