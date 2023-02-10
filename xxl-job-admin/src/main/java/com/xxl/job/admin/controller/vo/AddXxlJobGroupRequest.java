package com.xxl.job.admin.controller.vo;

import org.hibernate.validator.constraints.Length;
import org.springframework.util.StringUtils;

import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.NotBlank;

public class AddXxlJobGroupRequest {

    @NotBlank
    @Length(min = 4, max = 64)
    private String appname;
    @NotBlank
    private String title;
    private int addressType;        // 执行器地址类型：0=自动注册、1=手动录入
    private String addressList;     // 执行器地址列表，多地址逗号分隔(手动录入)

    public String getAppname() {
        return appname;
    }

    public void setAppname(String appname) {
        this.appname = appname;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getAddressType() {
        return addressType;
    }

    public void setAddressType(int addressType) {
        this.addressType = addressType;
    }

    public String getAddressList() {
        return addressList;
    }

    public void setAddressList(String addressList) {
        this.addressList = addressList;
    }

    @AssertFalse(message = "手动注册时，地址不能为空!")
    public Boolean check() {
        if (addressType == 1 && !StringUtils.hasText(addressList)) {
            return false;
        }
        return true;
    }
}
