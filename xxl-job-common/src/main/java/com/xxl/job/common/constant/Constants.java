package com.xxl.job.common.constant;

public class Constants {

    public static final long PRE_READ_MS = 5000;

    // 请求admin的token
    public static final String XXL_JOB_ACCESS_TOKEN = "XXL-JOB-ACCESS-TOKEN";

    // 需要校验token正确性的路径
    public static final String NEED_CHECK_TOKEN_URI = "/xxl-job-admin/api";

    // 请求admin接口的超时时间
    public static final int REQUEST_TIME_OUT = 3;
}
