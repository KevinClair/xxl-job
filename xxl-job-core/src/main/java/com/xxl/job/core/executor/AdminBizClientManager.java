package com.xxl.job.core.executor;

import com.xxl.job.core.biz.AdminBiz;
import com.xxl.job.core.biz.client.AdminBizClient;
import com.xxl.job.core.executor.config.XxlJobConfiguration;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Admin客户端管理
 */
public class AdminBizClientManager {

    private List<AdminBiz> adminBizList;

    public AdminBizClientManager(XxlJobConfiguration configuration) {
        this.initClients(configuration);
    }

    /**
     * 初始化admin客户端地址
     *
     * @param configuration 配置数据
     */
    public void initClients(XxlJobConfiguration configuration) {
        for (String address : configuration.getAddress().trim().split(",")) {
            if (StringUtils.hasLength(address)) {
                AdminBiz adminBiz = new AdminBizClient(address.trim(), configuration.getAccessToken());
                if (Objects.isNull(adminBizList)) {
                    adminBizList = new ArrayList<AdminBiz>();
                }
                adminBizList.add(adminBiz);
            }
        }
    }

    public List<AdminBiz> getAdminBizList() {
        return adminBizList;
    }
}
