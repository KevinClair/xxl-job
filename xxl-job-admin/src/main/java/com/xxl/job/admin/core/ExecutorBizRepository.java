package com.xxl.job.admin.core;

import com.xxl.job.admin.core.conf.XxlJobAdminConfig;
import com.xxl.job.core.biz.ExecutorBiz;
import com.xxl.job.core.biz.client.ExecutorBizClient;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * TODO
 *
 * @author KevinClair
 **/
@Component
public class ExecutorBizRepository {

    @Resource
    private XxlJobAdminConfig jobAdminConfig;

    private static ConcurrentMap<String, ExecutorBiz> executorBizRepository = new ConcurrentHashMap<>();

    public ExecutorBiz getExecutorBiz(String address) throws Exception {
        // valid
        if (!StringUtils.hasLength(address)) {
            return null;
        }

        // load-cache
        address = address.trim();
        return executorBizRepository.computeIfAbsent(address, k -> new ExecutorBizClient(k, jobAdminConfig.getAccessToken()));
    }
}
