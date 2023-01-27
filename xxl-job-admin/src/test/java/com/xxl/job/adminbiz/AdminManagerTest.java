package com.xxl.job.adminbiz;

import com.xxl.job.common.model.HandleCallbackParam;
import com.xxl.job.common.model.RegistryParam;
import com.xxl.job.common.model.ReturnT;
import com.xxl.job.common.service.AdminManager;
import com.xxl.job.core.biz.client.AdminManagerClient;
import com.xxl.job.core.context.XxlJobContext;
import com.xxl.job.core.enums.RegistryConfig;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * admin api test
 *
 * @author xuxueli 2017-07-28 22:14:52
 */
public class AdminManagerTest {

    // admin-client
    private static String addressUrl = "http://127.0.0.1:8080/xxl-job-admin/";
    private static String accessToken = null;


    @Test
    public void callback() throws Exception {
        AdminManager adminManager = new AdminManagerClient(addressUrl, accessToken);

        HandleCallbackParam param = new HandleCallbackParam();
        param.setLogId(1);
        param.setHandleCode(XxlJobContext.HANDLE_CODE_SUCCESS);

        List<HandleCallbackParam> callbackParamList = Arrays.asList(param);

        ReturnT<String> returnT = adminManager.callback(callbackParamList);

        assertTrue(returnT.getCode() == ReturnT.SUCCESS_CODE);
    }

    /**
     * registry executor
     *
     * @throws Exception
     */
    @Test
    public void registry() throws Exception {
        AdminManager adminManager = new AdminManagerClient(addressUrl, accessToken);

        RegistryParam registryParam = new RegistryParam(RegistryConfig.RegistType.EXECUTOR.name(), "xxl-job-executor-example", "127.0.0.1:9999");
        ReturnT<String> returnT = adminManager.registry(registryParam);

        assertTrue(returnT.getCode() == ReturnT.SUCCESS_CODE);
    }

    /**
     * registry executor remove
     *
     * @throws Exception
     */
    @Test
    public void registryRemove() throws Exception {
        AdminManager adminManager = new AdminManagerClient(addressUrl, accessToken);

        RegistryParam registryParam = new RegistryParam(RegistryConfig.RegistType.EXECUTOR.name(), "xxl-job-executor-example", "127.0.0.1:9999");
        ReturnT<String> returnT = adminManager.registryRemove(registryParam);

        assertTrue(returnT.getCode() == ReturnT.SUCCESS_CODE);

    }

}
