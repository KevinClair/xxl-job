package com.xxl.job.admin.core;

import com.xxl.job.common.constant.Constants;
import com.xxl.job.common.model.*;
import com.xxl.job.common.service.ExecutorManager;
import com.xxl.job.common.utils.XxlJobRemotingUtil;

/**
 * 执行器管理的Admin端实现，请求的是core包中通过Netty实现的http服务器，core的http服务器会根据不用的路径，响应操作
 *
 * @author xuxueli 2017-07-28 22:14:52
 */
public class ExecutorManagerClient implements ExecutorManager {

    private String executorUrl;
    private String accessToken;

    private static final String EXECUTOR_URL_BEAT = "/beat";
    private static final String EXECUTOR_URL_IDLE_BEAT = "/idleBeat";
    private static final String EXECUTOR_URL_RUN = "/run";
    private static final String EXECUTOR_URL_KILL = "/kill";
    private static final String EXECUTOR_URL_LOG = "/log";

    public ExecutorManagerClient(String executorUrl, String accessToken) {
        this.executorUrl = executorUrl;
        this.accessToken = accessToken;
    }

    @Override
    public ReturnT<String> beat() {
        return XxlJobRemotingUtil.postBody(executorUrl + EXECUTOR_URL_BEAT, accessToken, Constants.REQUEST_TIME_OUT, "", String.class);
    }

    @Override
    public ReturnT<String> idleBeat(IdleBeatParam idleBeatParam) {
        return XxlJobRemotingUtil.postBody(executorUrl + EXECUTOR_URL_IDLE_BEAT, accessToken, Constants.REQUEST_TIME_OUT, idleBeatParam, String.class);
    }

    @Override
    public ReturnT<String> run(TriggerParam triggerParam) {
        return XxlJobRemotingUtil.postBody(executorUrl + EXECUTOR_URL_RUN, accessToken, Constants.REQUEST_TIME_OUT, triggerParam, String.class);
    }

    @Override
    public ReturnT<String> kill(KillParam killParam) {
        return XxlJobRemotingUtil.postBody(executorUrl + EXECUTOR_URL_KILL, accessToken, Constants.REQUEST_TIME_OUT, killParam, String.class);
    }

    @Override
    public ReturnT<LogResult> log(LogParam logParam) {
        return XxlJobRemotingUtil.postBody(executorUrl + EXECUTOR_URL_LOG, accessToken, Constants.REQUEST_TIME_OUT, logParam, LogResult.class);
    }

}
