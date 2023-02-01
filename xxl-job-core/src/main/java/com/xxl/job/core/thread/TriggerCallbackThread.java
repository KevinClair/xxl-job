package com.xxl.job.core.thread;

import com.xxl.job.common.context.XxlJobContext;
import com.xxl.job.common.enums.RegistryConstants;
import com.xxl.job.common.model.HandleCallbackParam;
import com.xxl.job.common.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.executor.AdminManagerClientWrapper;
import com.xxl.job.core.handler.HandleCallbackParamRepository;
import com.xxl.job.core.log.XxlJobFileAppender;
import com.xxl.job.core.util.FileUtil;
import com.xxl.job.core.util.JdkSerializeTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by xuxueli on 16/7/22.
 */
public class TriggerCallbackThread implements DisposableBean {
    private static Logger logger = LoggerFactory.getLogger(TriggerCallbackThread.class);

    private final AdminManagerClientWrapper adminManagerClientWrapper;

    /**
     * callback thread
     */
    private final Thread triggerCallbackThread;

    private final ScheduledThreadPoolExecutor retryCallbackThreadPoolExecutor;
    private volatile boolean toStop = false;

    public TriggerCallbackThread(AdminManagerClientWrapper adminManagerClientWrapper) {
        this.adminManagerClientWrapper = adminManagerClientWrapper;
        this.triggerCallbackThread = new Thread(() -> {

            // normal callback
            while (!toStop) {
                try {
                    HandleCallbackParam callback = HandleCallbackParamRepository.take();

                    // callback list param
                    List<HandleCallbackParam> callbackParamList = new ArrayList<HandleCallbackParam>();
                    HandleCallbackParamRepository.drainTo(callbackParamList);
                    callbackParamList.add(callback);

                    // callback, will retry if error
                    if (!CollectionUtils.isEmpty(callbackParamList)) {
                        doCallback(callbackParamList);
                    }
                } catch (Exception e) {
                    if (!toStop) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }

            // last callback
            try {
                List<HandleCallbackParam> callbackParamList = new ArrayList<HandleCallbackParam>();
                HandleCallbackParamRepository.drainTo(callbackParamList);
                if (!CollectionUtils.isEmpty(callbackParamList)) {
                    doCallback(callbackParamList);
                }
            } catch (Exception e) {
                if (!toStop) {
                    logger.error(e.getMessage(), e);
                }
            }
            logger.info(">>>>>>>>>>> xxl-job, executor callback thread destroy.");

        });
        this.triggerCallbackThread.setDaemon(true);
        this.triggerCallbackThread.setName("xxl-job, executor TriggerCallbackThread");
        this.triggerCallbackThread.start();

        this.retryCallbackThreadPoolExecutor = new ScheduledThreadPoolExecutor(1, r -> new Thread(r, "xxl-job trigger retry callback thread"));
        this.retryCallbackThreadPoolExecutor.scheduleWithFixedDelay(() -> {
            try {
                retryFailCallbackFile();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }, 0, RegistryConstants.BEAT_TIMEOUT, TimeUnit.SECONDS);
    }

    /**
     * do callback, will retry if error
     *
     * @param callbackParamList
     */
    private void doCallback(List<HandleCallbackParam> callbackParamList) {
        boolean callbackRet = false;
        // callback, will retry if error
        try {
            ReturnT<String> callbackResult = adminManagerClientWrapper.getAdminManager().callback(callbackParamList);
            if (callbackResult != null && ReturnT.SUCCESS_CODE == callbackResult.getCode()) {
                callbackLog(callbackParamList, "<br>----------- xxl-job job callback finish.");
                callbackRet = true;
            } else {
                callbackLog(callbackParamList, "<br>----------- xxl-job job callback fail, callbackResult:" + callbackResult);
            }
        } catch (Exception e) {
            callbackLog(callbackParamList, "<br>----------- xxl-job job callback error, errorMsg:" + e.getMessage());
        }
        if (!callbackRet) {
            appendFailCallbackFile(callbackParamList);
        }
    }

    /**
     * callback log
     */
    private void callbackLog(List<HandleCallbackParam> callbackParamList, String logContent) {
        for (HandleCallbackParam callbackParam : callbackParamList) {
            String logFileName = XxlJobFileAppender.makeLogFileName(new Date(callbackParam.getLogDateTim()), callbackParam.getLogId());
            XxlJobContext.setXxlJobContext(new XxlJobContext(
                    -1,
                    null,
                    logFileName,
                    -1,
                    -1));
            XxlJobHelper.log(logContent);
        }
    }


    // ---------------------- fail-callback file ----------------------

    private static String failCallbackFilePath = XxlJobFileAppender.getLogPath().concat(File.separator).concat("callbacklog").concat(File.separator);
    private static String failCallbackFileName = failCallbackFilePath.concat("xxl-job-callback-{x}").concat(".log");

    private void appendFailCallbackFile(List<HandleCallbackParam> callbackParamList) {
        // valid
        if (CollectionUtils.isEmpty(callbackParamList)) {
            return;
        }
        // append file
        byte[] callbackParamList_bytes = JdkSerializeTool.serialize(callbackParamList);

        File callbackLogFile = new File(failCallbackFileName.replace("{x}", String.valueOf(System.currentTimeMillis())));
        if (callbackLogFile.exists()) {
            for (int i = 0; i < 100; i++) {
                callbackLogFile = new File(failCallbackFileName.replace("{x}", String.valueOf(System.currentTimeMillis()).concat("-").concat(String.valueOf(i))));
                if (!callbackLogFile.exists()) {
                    break;
                }
            }
        }
        FileUtil.writeFileContent(callbackLogFile, callbackParamList_bytes);
    }

    private void retryFailCallbackFile() {

        // valid
        File callbackLogPath = new File(failCallbackFilePath);
        if (!callbackLogPath.exists()) {
            return;
        }
        if (callbackLogPath.isFile()) {
            callbackLogPath.delete();
        }
        if (!(callbackLogPath.isDirectory() && callbackLogPath.list() != null && callbackLogPath.list().length > 0)) {
            return;
        }

        // load and clear file, retry
        for (File callbaclLogFile : callbackLogPath.listFiles()) {
            byte[] callbackParamList_bytes = FileUtil.readFileContent(callbaclLogFile);

            // avoid empty file
            if (callbackParamList_bytes == null || callbackParamList_bytes.length < 1) {
                callbaclLogFile.delete();
                continue;
            }

            List<HandleCallbackParam> callbackParamList = (List<HandleCallbackParam>) JdkSerializeTool.deserialize(callbackParamList_bytes, List.class);

            callbaclLogFile.delete();
            doCallback(callbackParamList);
        }
    }

    @Override
    public void destroy() throws Exception {
        toStop = true;
        // stop callback, interrupt and wait
        if (triggerCallbackThread != null) {    // support empty admin address
            triggerCallbackThread.interrupt();
            try {
                triggerCallbackThread.join();
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }

        // stop retry, interrupt and wait
        this.retryCallbackThreadPoolExecutor.shutdown();
        try {
            if (retryCallbackThreadPoolExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                logger.info(">>>>>>>>>>> xxl-job retryCallbackThreadPoolExecutor shutdown success.");
            }
        } catch (InterruptedException exception) {
            logger.error(">>>>>>>>>>> xxl-job retryCallbackThreadPoolExecutor shutdown error.", exception);
        }
    }
}
