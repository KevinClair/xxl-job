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
public class ExecutorBizRepository {

    private final XxlJobAdminConfig jobAdminConfig;

    public ExecutorBizRepository(XxlJobAdminConfig jobAdminConfig) {
        this.jobAdminConfig = jobAdminConfig;
    }

    private static ConcurrentMap<String, ExecutorManager> executorBizRepository = new ConcurrentHashMap<>();

    public ExecutorManager getExecutorBiz(String address) throws Exception {
        // valid
        if (!StringUtils.hasLength(address)) {
            return null;
        }

        // load-cache
        address = address.trim();
        return executorBizRepository.computeIfAbsent(address, k -> new ExecutorManagerClient(k, jobAdminConfig.getAccessToken()));
    }
}
