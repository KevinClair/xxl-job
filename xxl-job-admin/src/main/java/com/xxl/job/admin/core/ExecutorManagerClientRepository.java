package com.xxl.job.admin.core;

import com.xxl.job.admin.core.conf.XxlJobAdminConfig;
import com.xxl.job.common.service.ExecutorManager;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author KevinClair
 **/
@Component
public class ExecutorManagerClientRepository {

    private final XxlJobAdminConfig jobAdminConfig;

    public ExecutorManagerClientRepository(XxlJobAdminConfig jobAdminConfig) {
        this.jobAdminConfig = jobAdminConfig;
    }

    private static ConcurrentMap<String, ExecutorManager> executorBizRepository = new ConcurrentHashMap<>();

    public ExecutorManager getExecutorManagerClient(String address) {
        // valid
        if (!StringUtils.hasLength(address)) {
            return null;
        }

        // load-cache
        address = address.trim();
        return executorBizRepository.computeIfAbsent(address, k -> new ExecutorManagerClient(k, jobAdminConfig.getAccessToken()));
    }

    /**
     * 根据地址删除ExecutorManager
     *
     * @param address 执行器地址
     */
    public void remove(String address) {
        executorBizRepository.remove(address);
    }
}
