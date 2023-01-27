package com.xxl.job.core.executor;

import com.xxl.job.common.service.AdminManager;
import com.xxl.job.core.biz.client.AdminManagerClient;
import com.xxl.job.core.executor.config.XxlJobConfiguration;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin客户端管理
 */
public class AdminBizClientManager {

    private final List<AdminManager> adminManagerList = new ArrayList<AdminManager>();

    public AdminBizClientManager(XxlJobConfiguration configuration) {
        for (String address : configuration.getAddress().trim().split(",")) {
            if (StringUtils.hasLength(address)) {
                AdminManager adminManager = new AdminManagerClient(address.trim(), configuration.getAccessToken());
                adminManagerList.add(adminManager);
            }
        }
    }

    public List<AdminManager> getAdminBizList() {
        return adminManagerList;
    }
}
