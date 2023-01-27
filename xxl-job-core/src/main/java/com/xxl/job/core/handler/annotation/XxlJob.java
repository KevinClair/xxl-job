package com.xxl.job.core.handler.annotation;

import java.lang.annotation.*;

/**
 * annotation for method jobhandler
 *
 * @author xuxueli 2019-12-11 20:50:13
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface XxlJob {

    /**
     * jobhandler name
     */
    String value();

    /**
     * init handler, invoked when JobThread init
     */
    String init() default "";

    /**
     * destroy handler, invoked when JobThread destroy
     */
    String destroy() default "";

    /**
     * 是否需要自动创建任务
     * TODO 自动生成任务
     *
     * @return
     */
    boolean autoCreated() default false;

}
