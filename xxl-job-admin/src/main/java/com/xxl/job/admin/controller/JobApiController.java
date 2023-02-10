package com.xxl.job.admin.controller;

import com.xxl.job.admin.controller.annotation.PermissionLimit;
import com.xxl.job.common.dto.AddXxlJobInfoDto;
import com.xxl.job.common.dto.DeleteXxlJobInfoDto;
import com.xxl.job.common.dto.SaveXxlJobInfoDto;
import com.xxl.job.common.dto.UpdateXxlJobInfoDto;
import com.xxl.job.common.model.HandleCallbackParam;
import com.xxl.job.common.model.RegistryParam;
import com.xxl.job.common.model.ReturnT;
import com.xxl.job.common.service.AdminManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Created by xuxueli on 17/5/10.
 */
@Controller
@RequestMapping("/api")
public class JobApiController {

    private final AdminManager adminManager;

    public JobApiController(AdminManager adminManager) {
        this.adminManager = adminManager;
    }

    @PostMapping("/callback")
    @PermissionLimit(limit = false)
    @ResponseBody
    public ReturnT<String> callback(@RequestBody @NotEmpty List<HandleCallbackParam> data) {
        return adminManager.callback(data);
    }

    @PostMapping("/registry")
    @PermissionLimit(limit = false)
    @ResponseBody
    public ReturnT<String> registry(@RequestBody @Valid RegistryParam data) {
        return adminManager.registry(data);
    }

    @PostMapping("/registryRemove")
    @PermissionLimit(limit = false)
    @ResponseBody
    public ReturnT<String> registryRemove(@RequestBody @Valid RegistryParam data) {
        return adminManager.registryRemove(data);
    }

    @PostMapping("/addJob")
    @PermissionLimit(limit = false)
    @ResponseBody
    public ReturnT<String> addJob(@RequestBody @Valid AddXxlJobInfoDto data) {
        return adminManager.addJob(data);
    }

    @PostMapping("/deleteJob")
    @PermissionLimit(limit = false)
    @ResponseBody
    public ReturnT<String> deleteJob(@RequestBody @Valid DeleteXxlJobInfoDto data) {
        return adminManager.deleteJob(data);
    }

    @PostMapping("/updateJob")
    @PermissionLimit(limit = false)
    @ResponseBody
    public ReturnT<String> updateJob(@RequestBody @Valid UpdateXxlJobInfoDto data) {
        return adminManager.updateJob(data);
    }

    @PostMapping("/saveJob")
    @PermissionLimit(limit = false)
    @ResponseBody
    public ReturnT<String> saveJob(@RequestBody @Valid SaveXxlJobInfoDto data) {
        return adminManager.saveJob(data);
    }
}
