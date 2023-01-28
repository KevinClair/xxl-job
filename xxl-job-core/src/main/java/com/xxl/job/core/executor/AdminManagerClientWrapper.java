package com.xxl.job.core.executor;

import com.xxl.job.common.model.RegistryParam;
import com.xxl.job.common.service.AdminManager;
import com.xxl.job.core.biz.client.AdminManagerClient;
import com.xxl.job.core.executor.config.XxlJobConfiguration;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin客户端管理
 * <p>
 * 客户端(应用端)如果遇到Admin集群部署时，{@link XxlJobConfiguration}中的属性address会写多个，但经过分析认为只需要填写集群中的一个地址即可，或者填写https地址
 * 1.{@link AdminManager}中的操作对于Admin来说，底层逻辑都是基于数据库操作，因此没有必要把集群中的所有地址都填上(初步判断作者可能认为如果其中一个地址处理失效，还可以通过其他地址完成操作)；
 * 2.在第一个条件存在下，对于某一个操作，例如{@link AdminManager#registry(RegistryParam)},如果填写多个地址，相当于对数据库操作了多次，不太合理；
 * 3.这里改为如果Admin集群部署时，地址填写多个，只使用其中一个。
 * 4.为了方便后续修改，考虑在此类中包装一次操作，如果后续还需要修改为填写多个地址时，可以回退
 */
public class AdminManagerClientWrapper {

    private final List<AdminManager> adminManagerList = new ArrayList<AdminManager>();

    public AdminManagerClientWrapper(XxlJobConfiguration configuration) {
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
