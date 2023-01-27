package com.xxl.job.core.biz.client;

import com.xxl.job.common.constant.Constants;
import com.xxl.job.common.model.ReturnT;
import com.xxl.job.core.biz.ExecutorBiz;
import com.xxl.job.core.biz.model.*;
import com.xxl.job.core.util.XxlJobRemotingUtil;

/**
 * admin api test
 *
 * @author xuxueli 2017-07-28 22:14:52
 */
public class ExecutorBizClient implements ExecutorBiz {

    private String addressUrl;
    private String accessToken;
    
    private static final String ADMIN_URL_BEAT = "/beat";
    private static final String ADMIN_URL_IDLE_BEAT = "/idleBeat";
    private static final String ADMIN_URL_RUN = "/run";
    private static final String ADMIN_URL_KILL = "/kill";
    private static final String ADMIN_URL_LOG = "/log";

    public ExecutorBizClient(String addressUrl, String accessToken) {
        this.addressUrl = addressUrl;
        this.accessToken = accessToken;
    }

    @Override
    public ReturnT<String> beat() {
        return XxlJobRemotingUtil.postBody(addressUrl + ADMIN_URL_BEAT, accessToken, Constants.ADMIN_REQUEST_TIME_OUT, "", String.class);
    }

    @Override
    public ReturnT<String> idleBeat(IdleBeatParam idleBeatParam) {
        return XxlJobRemotingUtil.postBody(addressUrl + ADMIN_URL_IDLE_BEAT, accessToken, Constants.ADMIN_REQUEST_TIME_OUT, idleBeatParam, String.class);
    }

    @Override
    public ReturnT<String> run(TriggerParam triggerParam) {
        return XxlJobRemotingUtil.postBody(addressUrl + ADMIN_URL_RUN, accessToken, Constants.ADMIN_REQUEST_TIME_OUT, triggerParam, String.class);
    }

    @Override
    public ReturnT<String> kill(KillParam killParam) {
        return XxlJobRemotingUtil.postBody(addressUrl + ADMIN_URL_KILL, accessToken, Constants.ADMIN_REQUEST_TIME_OUT, killParam, String.class);
    }

    @Override
    public ReturnT<LogResult> log(LogParam logParam) {
        return XxlJobRemotingUtil.postBody(addressUrl + ADMIN_URL_LOG, accessToken, Constants.ADMIN_REQUEST_TIME_OUT, logParam, LogResult.class);
    }

}
