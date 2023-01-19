package com.xxl.job.admin.core.route;

import com.xxl.job.common.utils.I18nUtil;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.biz.model.TriggerParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Created by xuxueli on 17/3/10.
 */
public abstract class AbstractExecutorRouter implements ExecutorRouter {
    protected static Logger logger = LoggerFactory.getLogger(AbstractExecutorRouter.class);

    @Override
    public ReturnT<String> route(TriggerParam triggerParam, List<String> addressList) {
        if (CollectionUtils.isEmpty(addressList)){
            return new ReturnT<>(ReturnT.FAIL_CODE, I18nUtil.getString("jobconf_trigger_address_empty"));
        }
        if (addressList.size() == 1){
            return new ReturnT<>(addressList.get(0));
        }
        return routeAddress(triggerParam, addressList);
    }

    /**
     * route address
     *
     * @param addressList
     * @return  ReturnT.content=address
     */
    protected abstract ReturnT<String> routeAddress(TriggerParam triggerParam, List<String> addressList);

}
