package com.xxl.job.common.enums;

/**
 * Created by xuxueli on 17/5/10.
 */
public class RegistryConstants {

    public static final int BEAT_TIMEOUT = 30;
    public static final int DEAD_TIMEOUT = BEAT_TIMEOUT * 3;

    public enum RegistryType {EXECUTOR, ADMIN}

}