package com.xxl.job.admin.core.route.strategy;

import com.xxl.job.admin.core.ExecutorBizRepository;
import com.xxl.job.admin.core.route.AbstractExecutorRouter;
import com.xxl.job.common.model.IdleBeatParam;
import com.xxl.job.common.model.ReturnT;
import com.xxl.job.common.model.TriggerParam;
import com.xxl.job.common.service.ExecutorManager;
import com.xxl.job.common.utils.I18nUtil;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by xuxueli on 17/3/10.
 */
@Component("executorRouteBusyover")
public class ExecutorRouteBusyover extends AbstractExecutorRouter {

    private final ExecutorBizRepository executorBizRepository;

    public ExecutorRouteBusyover(ExecutorBizRepository executorBizRepository) {
        this.executorBizRepository = executorBizRepository;
    }

    @Override
    protected ReturnT<String> routeAddress(TriggerParam triggerParam, List<String> addressList) {
        StringBuffer idleBeatResultSB = new StringBuffer();
        for (String address : addressList) {
            // beat
            ReturnT<String> idleBeatResult = null;
            try {
                ExecutorManager executorManager = executorBizRepository.getExecutorBiz(address);
                idleBeatResult = executorManager.idleBeat(new IdleBeatParam(triggerParam.getJobId()));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                idleBeatResult = new ReturnT<String>(ReturnT.FAIL_CODE, ""+e );
            }
            idleBeatResultSB.append( (idleBeatResultSB.length()>0)?"<br><br>":"")
                    .append(I18nUtil.getString("jobconf_idleBeat") + "：")
                    .append("<br>address：").append(address)
                    .append("<br>code：").append(idleBeatResult.getCode())
                    .append("<br>msg：").append(idleBeatResult.getMsg());

            // beat success
            if (idleBeatResult.getCode() == ReturnT.SUCCESS_CODE) {
                idleBeatResult.setMsg(idleBeatResultSB.toString());
                idleBeatResult.setContent(address);
                return idleBeatResult;
            }
        }

        return new ReturnT<String>(ReturnT.FAIL_CODE, idleBeatResultSB.toString());
    }

}
