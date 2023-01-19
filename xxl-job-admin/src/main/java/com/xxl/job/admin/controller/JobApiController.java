package com.xxl.job.admin.controller;

import com.xxl.job.admin.controller.annotation.PermissionLimit;
import com.xxl.job.admin.core.conf.XxlJobAdminConfig;
import com.xxl.job.admin.core.exception.XxlJobException;
import com.xxl.job.common.constant.Constants;
import com.xxl.job.core.biz.AdminBiz;
import com.xxl.job.core.biz.model.HandleCallbackParam;
import com.xxl.job.core.biz.model.RegistryParam;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.util.GsonTool;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by xuxueli on 17/5/10.
 */
@Controller
@RequestMapping("/api")
public class JobApiController {

    private final AdminBiz adminBiz;

    private final XxlJobAdminConfig adminConfig;

    public JobApiController(AdminBiz adminBiz, XxlJobAdminConfig adminConfig) {
        this.adminBiz = adminBiz;
        this.adminConfig = adminConfig;
    }

    /**
     * api
     *
     * @param uri
     * @param data
     * @return
     */
    @RequestMapping("/{uri}")
    @PermissionLimit(limit = false)
    public ReturnT<String> api(HttpServletRequest request, @PathVariable("uri") String uri, @RequestBody(required = false) String data) {

        // valid
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "invalid request, HttpMethod not support.");
        }
        if (uri == null || uri.trim().length() == 0) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "invalid request, uri-mapping empty.");
        }
        if (adminConfig.getAccessToken() != null
                && adminConfig.getAccessToken().trim().length() > 0
                && !adminConfig.getAccessToken().equals(request.getHeader(Constants.XXL_JOB_ACCESS_TOKEN))) {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "The access token is wrong.");
        }

        // services mapping
        if ("callback".equals(uri)) {
            List<HandleCallbackParam> callbackParamList = GsonTool.fromJson(data, List.class, HandleCallbackParam.class);
            return adminBiz.callback(callbackParamList);
        } else if ("registry".equals(uri)) {
            RegistryParam registryParam = GsonTool.fromJson(data, RegistryParam.class);
            return adminBiz.registry(registryParam);
        } else if ("registryRemove".equals(uri)) {
            RegistryParam registryParam = GsonTool.fromJson(data, RegistryParam.class);
            return adminBiz.registryRemove(registryParam);
        } else {
            return new ReturnT<String>(ReturnT.FAIL_CODE, "invalid request, uri-mapping(" + uri + ") not found.");
        }

    }

    @PostMapping("/callback")
    @PermissionLimit(limit = false)
    public ReturnT<String> callback(HttpServletRequest request, @RequestBody List<HandleCallbackParam> data) {
        this.checkAccessToken(request);
        return adminBiz.callback(data);
    }

    @PostMapping("/registry")
    @PermissionLimit(limit = false)
    public ReturnT<String> registry(HttpServletRequest request, @RequestBody RegistryParam data) {
        this.checkAccessToken(request);
        return adminBiz.registry(data);
    }

    @PostMapping("/registryRemove")
    @PermissionLimit(limit = false)
    public ReturnT<String> registryRemove(HttpServletRequest request, @RequestBody RegistryParam data) {
        this.checkAccessToken(request);
        return adminBiz.registryRemove(data);
    }

    // TODO 添加job
    @PostMapping("/addJob")
    @PermissionLimit(limit = false)
    public ReturnT<String> addJob(HttpServletRequest request, @RequestBody(required = false) List<HandleCallbackParam> data) {
        this.checkAccessToken(request);
        return adminBiz.callback(data);
    }

    // TODO 删除job
    @PostMapping("/deleteJob")
    @PermissionLimit(limit = false)
    public ReturnT<String> deleteJob(HttpServletRequest request, @RequestBody(required = false) List<HandleCallbackParam> data) {
        this.checkAccessToken(request);
        return adminBiz.callback(data);
    }

    /**
     * 校验AccessToken合法性
     *
     * @param request {@link HttpServletRequest}
     */
    private void checkAccessToken(HttpServletRequest request) {
        if (StringUtils.hasText(adminConfig.getAccessToken()) && !adminConfig.getAccessToken().equals(request.getHeader(Constants.XXL_JOB_ACCESS_TOKEN))) {
            throw new XxlJobException("The access token is wrong.");
        }
    }

}
