package com.xxl.job.admin.controller;

import com.xxl.job.admin.controller.annotation.PermissionLimit;
import com.xxl.job.admin.core.conf.XxlJobAdminConfig;
import com.xxl.job.admin.core.exception.XxlJobException;
import com.xxl.job.common.constant.Constants;
import com.xxl.job.common.dto.AddXxlJobInfoDto;
import com.xxl.job.common.dto.DeleteXxlJobInfoDto;
import com.xxl.job.common.dto.SaveXxlJobInfoDto;
import com.xxl.job.common.dto.UpdateXxlJobInfoDto;
import com.xxl.job.common.model.HandleCallbackParam;
import com.xxl.job.common.model.RegistryParam;
import com.xxl.job.common.model.ReturnT;
import com.xxl.job.common.service.AdminManager;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by xuxueli on 17/5/10.
 */
@Controller
@RequestMapping("/api")
public class JobApiController {

    private final AdminManager adminManager;

    private final XxlJobAdminConfig adminConfig;

    public JobApiController(AdminManager adminManager, XxlJobAdminConfig adminConfig) {
        this.adminManager = adminManager;
        this.adminConfig = adminConfig;
    }

    @PostMapping("/callback")
    @PermissionLimit(limit = false)
    @ResponseBody
    public ReturnT<String> callback(HttpServletRequest request, @RequestBody List<HandleCallbackParam> data) {
        this.checkAccessToken(request);
        return adminManager.callback(data);
    }

    @PostMapping("/registry")
    @PermissionLimit(limit = false)
    @ResponseBody
    public ReturnT<String> registry(HttpServletRequest request, @RequestBody RegistryParam data) {
        this.checkAccessToken(request);
        return adminManager.registry(data);
    }

    @PostMapping("/registryRemove")
    @PermissionLimit(limit = false)
    @ResponseBody
    public ReturnT<String> registryRemove(HttpServletRequest request, @RequestBody RegistryParam data) {
        this.checkAccessToken(request);
        return adminManager.registryRemove(data);
    }

    @PostMapping("/addJob")
    @PermissionLimit(limit = false)
    @ResponseBody
    public ReturnT<String> addJob(HttpServletRequest request, @RequestBody(required = false) AddXxlJobInfoDto data) {
        this.checkAccessToken(request);
        return adminManager.addJob(data);
    }

    @PostMapping("/deleteJob")
    @PermissionLimit(limit = false)
    @ResponseBody
    public ReturnT<String> deleteJob(HttpServletRequest request, @RequestBody(required = false) DeleteXxlJobInfoDto data) {
        this.checkAccessToken(request);
        return adminManager.deleteJob(data);
    }

    @PostMapping("/updateJob")
    @PermissionLimit(limit = false)
    @ResponseBody
    public ReturnT<String> updateJob(HttpServletRequest request, @RequestBody(required = false) UpdateXxlJobInfoDto data) {
        this.checkAccessToken(request);
        return adminManager.updateJob(data);
    }

    @PostMapping("/saveJob")
    @PermissionLimit(limit = false)
    @ResponseBody
    public ReturnT<String> saveJob(HttpServletRequest request, @RequestBody(required = false) SaveXxlJobInfoDto data) {
        this.checkAccessToken(request);
        return adminManager.saveJob(data);
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
