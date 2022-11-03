package com.xxl.job.core.handler;

import com.xxl.job.core.biz.model.HandleCallbackParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 */
public class HandleCallbackParamRepository {

    private static final Logger logger = LoggerFactory.getLogger(HandleCallbackParamRepository.class);

    /**
     * job results callback queue
     */
    private static LinkedBlockingQueue<HandleCallbackParam> callBackQueue = new LinkedBlockingQueue<>();

    /**
     * 向队列中推送数据
     *
     * @param callback
     */
    public static void pushCallBack(HandleCallbackParam callback){
        callBackQueue.add(callback);
        logger.debug(">>>>>>>>>>> xxl-job, push callback request, logId:{}", callback.getLogId());
    }

    public static HandleCallbackParam take() throws InterruptedException {
        return callBackQueue.take();
    }

    public static int drainTo(List<HandleCallbackParam> callbackParamList){
        return callBackQueue.drainTo(callbackParamList);
    }

    public static LinkedBlockingQueue<HandleCallbackParam> getCallBackQueue() {
        return callBackQueue;
    }
}
