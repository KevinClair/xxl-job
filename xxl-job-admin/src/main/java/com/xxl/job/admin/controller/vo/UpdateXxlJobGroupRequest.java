package com.xxl.job.admin.controller.vo;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class UpdateXxlJobGroupRequest {

    @NotNull
    private Integer id;
    @NotBlank
    private String title;

    @NotBlank// 执行器地址类型：0=自动注册、1=手动录入
    private String addressList;     // 执行器地址列表，多地址逗号分隔(手动录入)

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAddressList() {
        return addressList;
    }

    public void setAddressList(String addressList) {
        this.addressList = addressList;
    }
}
