package com.xxl.job.admin.core.route.strategy;

import com.xxl.job.admin.core.ExecutorBizRepository;
import com.xxl.job.admin.core.route.AbstractExecutorRouter;
import com.xxl.job.common.model.ReturnT;
import com.xxl.job.common.model.TriggerParam;
import com.xxl.job.common.service.ExecutorManager;
import com.xxl.job.common.utils.I18nUtil;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by xuxueli on 17/3/10.
 */
@Component("executorRouteFailover")
public class ExecutorRouteFailover extends AbstractExecutorRouter {

    private final ExecutorBizRepository executorBizRepository;

    public ExecutorRouteFailover(ExecutorBizRepository executorBizRepository) {
        this.executorBizRepository = executorBizRepository;
    }

    @Override
    public ReturnT<String> routeAddress(TriggerParam triggerParam, List<String> addressList) {

        StringBuffer beatResultSB = new StringBuffer();
        for (String address : addressList) {
            // beat
            ReturnT<String> beatResult = null;
            try {
                ExecutorManager executorManager = executorBizRepository.getExecutorBiz(address);
                beatResult = executorManager.beat();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                beatResult = new ReturnT<String>(ReturnT.FAIL_CODE, ""+e );
            }
            beatResultSB.append( (beatResultSB.length()>0)?"<br><br>":"")
                    .append(I18nUtil.getString("jobconf_beat") + "：")
                    .append("<br>address：").append(address)
                    .append("<br>code：").append(beatResult.getCode())
                    .append("<br>msg：").append(beatResult.getMsg());

            // beat success
            if (beatResult.getCode() == ReturnT.SUCCESS_CODE) {

                beatResult.setMsg(beatResultSB.toString());
                beatResult.setContent(address);
                return beatResult;
            }
        }
        return new ReturnT<String>(ReturnT.FAIL_CODE, beatResultSB.toString());

    }
}
