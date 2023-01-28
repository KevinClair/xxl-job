package com.xxl.job.common.service;

import com.xxl.job.common.model.*;

/**
 * 执行器管理接口
 * <p>
 * Created by xuxueli on 17/3/1.
 */
public interface ExecutorManager {

    /**
     * beat
     *
     * @return
     */
    ReturnT<String> beat();

    /**
     * idle beat
     *
     * @param idleBeatParam
     * @retur
     */
    ReturnT<String> idleBeat(IdleBeatParam idleBeatParam);

    /**
     * run
     * @param triggerParam
     * @return
     */
    ReturnT<String> run(TriggerParam triggerParam);

    /**
     * kill
     * @param killParam
     * @return
     */
    ReturnT<String> kill(KillParam killParam);

    /**
     * log
     * @param logParam
     * @return
     */
    ReturnT<LogResult> log(LogParam logParam);

}
