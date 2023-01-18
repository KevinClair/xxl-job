package com.xxl.job.core.executor;

import com.xxl.job.core.biz.AdminBiz;
import com.xxl.job.core.biz.client.AdminBizClient;
import com.xxl.job.core.executor.config.XxlJobConfiguration;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin客户端管理
 */
public class AdminBizClientManager {

    private final List<AdminBiz> adminBizList = new ArrayList<AdminBiz>();

    public AdminBizClientManager(XxlJobConfiguration configuration) {
        for (String address : configuration.getAddress().trim().split(",")) {
            if (StringUtils.hasLength(address)) {
                AdminBiz adminBiz = new AdminBizClient(address.trim(), configuration.getAccessToken());
                adminBizList.add(adminBiz);
            }
        }
    }

    public List<AdminBiz> getAdminBizList() {
        return adminBizList;
    }
}
