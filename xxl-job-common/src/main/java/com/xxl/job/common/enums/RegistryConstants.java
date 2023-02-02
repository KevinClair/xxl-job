package com.xxl.job.common.enums;

/**
 * Created by xuxueli on 17/5/10.
 */
public class RegistryConstants {

    public static final int BEAT_TIMEOUT = 30;
    public static final int DEAD_TIMEOUT = BEAT_TIMEOUT * 3;

    public static final int ZOMBIE_DEAD_TIMEOUT = DEAD_TIMEOUT * 2;


    public enum RegistryType {EXECUTOR, ADMIN}

}
