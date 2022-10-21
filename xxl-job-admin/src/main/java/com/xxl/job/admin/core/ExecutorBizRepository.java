package com.xxl.job.admin.core;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.xxl.job.admin.core.conf.XxlJobAdminConfig;
import com.xxl.job.core.biz.ExecutorBiz;
import com.xxl.job.core.biz.client.ExecutorBizClient;

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
        if (address==null || address.trim().length()==0) {
            return null;
        }

        // load-cache
        address = address.trim();
        ExecutorBiz executorBiz = executorBizRepository.get(address);
        if (executorBiz != null) {
            return executorBiz;
        }

        // set-cache
        executorBiz = new ExecutorBizClient(address, jobAdminConfig.getAccessToken());

        executorBizRepository.put(address, executorBiz);
        return executorBiz;
    }
}
