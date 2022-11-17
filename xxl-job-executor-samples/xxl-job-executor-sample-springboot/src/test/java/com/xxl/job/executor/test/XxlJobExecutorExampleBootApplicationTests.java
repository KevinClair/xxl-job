package com.xxl.job.executor.test;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XxlJobExecutorExampleBootApplicationTests {

    private static final Logger logger = LoggerFactory.getLogger(XxlJobExecutorExampleBootApplicationTests.class);

    private boolean flag = false;

    @Test
    public void test() throws InterruptedException {
        Thread thread = new Thread(() -> {
            while (!flag) {
                logger.info("线程运行中");
            }
            logger.info("标志位flag:"+flag+"，线程继续运行中");
            logger.info("当前线程状态：", Thread.currentThread().getState().name());
            try {
                Thread.sleep(1000);
                logger.info("内部线程睡眠结束了");
            } catch (InterruptedException e) {
                logger.info("当前线程状态：", Thread.currentThread().getState().name());
                logger.info("线程被停止了");
            }
            logger.info("当前线程状态：", Thread.currentThread().getState().name());
            logger.info("线程休眠结束");
        });

        thread.start();

        Thread.sleep(5000);
        flag = true;
        thread.interrupt();
        thread.join();
        logger.info("线程结束了");
    }

    @Test
    public void test2() throws InterruptedException {
        Thread thread = new Thread(() -> {
            while (true) {
                logger.info("当前线程状态1："+Thread.currentThread().getState().name());
                try {
                    Thread.sleep(1000);
                    logger.info("内部线程睡眠结束了");
                } catch (InterruptedException e) {
                    logger.info("当前线程状态2："+Thread.currentThread().getState().name());
                    logger.info("线程被停止了");
                }
            }
        });

        thread.start();

        Thread.sleep(5000);
        thread.interrupt();
        logger.info("当前线程状态3："+thread.getState().name());
        thread.join();
        logger.info("当前线程状态4："+thread.getState().name());
        logger.info("线程结束了");
    }

    @Test
    public void test3() throws InterruptedException {
        ScheduledThreadPoolExecutor threadPoolExecutor = new ScheduledThreadPoolExecutor(1);
        threadPoolExecutor.scheduleWithFixedDelay(() -> {
            while (true) {
                logger.info("当前线程状态1："+Thread.currentThread().getState().name());
                try {
                    Thread.sleep(1000);
                    logger.info("内部线程睡眠结束了");
                } catch (InterruptedException e) {
                    logger.info("当前线程状态2："+Thread.currentThread().getState().name());
                    logger.info("线程被停止了");
                }
            }
        }, 0, 100, TimeUnit.MILLISECONDS);

        Thread.sleep(5000);
        threadPoolExecutor.shutdownNow();
        logger.info("当前线程池状态3："+threadPoolExecutor.isTerminating());
        logger.info("当前线程池状态4："+threadPoolExecutor.isTerminated());
        logger.info("当前线程池状态5："+threadPoolExecutor.isShutdown());

        Thread.sleep(20000);
        logger.info("线程结束了");
    }

    @Test
    public void test4() throws InterruptedException {
        ScheduledThreadPoolExecutor threadPoolExecutor = new ScheduledThreadPoolExecutor(1);
        threadPoolExecutor.scheduleWithFixedDelay(() -> {
            logger.info("当前线程状态1：" + Thread.currentThread().getState().name());
            try {
                Thread.sleep(1000);
                logger.info("内部线程睡眠结束了");
            } catch (InterruptedException e) {
                logger.info("当前线程状态2：" + Thread.currentThread().getState().name());
                logger.info("线程被停止了");
            }
        }, 0, 100, TimeUnit.MILLISECONDS);

        Thread.sleep(5000);
        threadPoolExecutor.shutdownNow();
        logger.info("当前线程池状态3："+threadPoolExecutor.isTerminating());
        logger.info("当前线程池状态4："+threadPoolExecutor.isTerminated());
        logger.info("当前线程池状态5："+threadPoolExecutor.isShutdown());

        Thread.sleep(20000);
        logger.info("线程结束了");
    }

}