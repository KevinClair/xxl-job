package com.xxl.job.core.handler.annotation;

import com.xxl.job.common.enums.ExecutorBlockStrategyEnum;
import com.xxl.job.common.enums.ExecutorRouteStrategyEnum;
import com.xxl.job.common.enums.MisfireStrategyEnum;
import com.xxl.job.common.enums.ScheduleTypeEnum;

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
     *
     * @return
     */
    boolean autoCreated() default false;

    /**
     * 当通过jobDesc查询到有相同的执行器时，是否覆盖原有的属性；
     *
     * @return
     */
    boolean covered() default false;

    /**
     * 调度任务描述
     *
     * @return
     */
    String jobDesc() default "";

    /**
     * 负责人
     *
     * @return
     */
    String author() default "admin";

    /**
     * 报警邮件
     *
     * @return
     */
    String alarmEmail() default "admin@admin.com";

    /**
     * 调度类型,默认为corn
     *
     * @return
     */
    ScheduleTypeEnum scheduleType() default ScheduleTypeEnum.CRON;

    /**
     * 调度配置，值含义取决于调度类型。如果是corn类型，此处填写corn表达式
     *
     * @return
     */
    String scheduleConf() default "";

    /**
     * 执行器，任务参数
     *
     * @return
     */
    String executorParam() default "";

    /**
     * 执行器路由策略,默认为第一个
     *
     * @return
     */
    ExecutorRouteStrategyEnum executorRouteStrategy() default ExecutorRouteStrategyEnum.FIRST;

    /**
     * 子任务ID，多个逗号分隔
     *
     * @return
     */
    String childJobId() default "";

    /**
     * 调度过期策略。默认为忽略
     *
     * @return
     */
    MisfireStrategyEnum misfireStrategy() default MisfireStrategyEnum.DO_NOTHING;

    /**
     * 阻塞处理策略，默认为单机串行
     *
     * @return
     */
    ExecutorBlockStrategyEnum executorBlockStrategy() default ExecutorBlockStrategyEnum.SERIAL_EXECUTION;

    /**
     * 任务执行超时时间，单位秒
     *
     * @return
     */
    int executorTimeout() default 0;

    /**
     * 失败重试次数
     *
     * @return
     */
    int executorFailRetryCount() default 0;
}
