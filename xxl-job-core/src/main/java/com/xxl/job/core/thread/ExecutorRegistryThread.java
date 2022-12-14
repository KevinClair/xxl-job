package com.xxl.job.core.thread;

import com.xxl.job.core.biz.AdminBiz;
import com.xxl.job.core.biz.model.RegistryParam;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.enums.RegistryConfig;
import com.xxl.job.core.executor.AdminBizClientManager;
import com.xxl.job.core.executor.config.XxlJobConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by xuxueli on 17/3/2.
 */
public class ExecutorRegistryThread implements DisposableBean {
    private static Logger logger = LoggerFactory.getLogger(ExecutorRegistryThread.class);

    private final AdminBizClientManager bizClientManager;

    private final XxlJobConfiguration configuration;

    private final ScheduledThreadPoolExecutor executorRegistryThreadPool;

    public ExecutorRegistryThread(AdminBizClientManager bizClientManager, XxlJobConfiguration configuration) {
        this.bizClientManager = bizClientManager;
        this.configuration = configuration;
        this.executorRegistryThreadPool = new ScheduledThreadPoolExecutor(1, r -> new Thread(r, "xxl-job, executor ExecutorRegistryThread"));
    }

    /**
     * start registry job info.
     */
    public void startRegistry(){
        this.executorRegistryThreadPool.scheduleAtFixedRate(() -> {
            RegistryParam registryParam = new RegistryParam(RegistryConfig.RegistType.EXECUTOR.name(), configuration.getAppName(), configuration.getExecutorAddress());
            for (AdminBiz adminBiz : bizClientManager.getAdminBizList()) {
                try {
                    ReturnT<String> registryResult = adminBiz.registry(registryParam);
                    if (registryResult != null && ReturnT.SUCCESS_CODE == registryResult.getCode()) {
                        logger.debug(">>>>>>>>>>> xxl-job registry success, registryParam:{}, registryResult:{}", new Object[]{registryParam, ReturnT.SUCCESS});
                        break;
                    } else {
                        logger.info(">>>>>>>>>>> xxl-job registry fail, registryParam:{}, registryResult:{}", new Object[]{registryParam, registryResult});
                    }
                } catch (Exception e) {
                    logger.error(">>>>>>>>>>> xxl-job registry error, registryParam:{}", registryParam, e);
                }

            }
        }, 0, RegistryConfig.BEAT_TIMEOUT, TimeUnit.SECONDS);
    }

    @Override
    public void destroy() throws Exception {
        // stop thread pool
        executorRegistryThreadPool.shutdownNow();
        try {
            if (executorRegistryThreadPool.awaitTermination(5, TimeUnit.SECONDS)){
                logger.info(">>>>>>>>>>> xxl-job executorRegistryThreadPool shutdown success.");
            }
        } catch (InterruptedException exception){
            logger.error(">>>>>>>>>>> xxl-job executorRegistryThreadPool shutdown error.", exception);
        }
        RegistryParam registryParam = new RegistryParam(RegistryConfig.RegistType.EXECUTOR.name(), configuration.getAppName(), configuration.getExecutorAddress());
        for (AdminBiz adminBiz : bizClientManager.getAdminBizList()) {
            try {
                ReturnT<String> registryResult = adminBiz.registryRemove(registryParam);
                if (registryResult != null && ReturnT.SUCCESS_CODE == registryResult.getCode()) {
                    logger.info(">>>>>>>>>>> xxl-job registry-remove success, registryParam:{}, registryResult:{}", new Object[]{registryParam, ReturnT.SUCCESS});
                    break;
                } else {
                    logger.info(">>>>>>>>>>> xxl-job registry-remove fail, registryParam:{}, registryResult:{}", new Object[]{registryParam, registryResult});
                }
            } catch (Exception e) {
                logger.error(">>>>>>>>>>> xxl-job registry-remove error, registryParam:{}", registryParam, e);
            }
        }
    }
}
