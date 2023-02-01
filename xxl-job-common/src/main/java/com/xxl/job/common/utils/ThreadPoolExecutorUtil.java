package com.xxl.job.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * 线程池工具类
 */
public class ThreadPoolExecutorUtil {

    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolExecutorUtil.class);
    private static final ThreadPoolExecutor SHUTDOWN_EXECUTOR = new ThreadPoolExecutor(0, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(100),
            new NamedThreadFactory("Close-ExecutorService-Timer", true));

    public static boolean isTerminated(Executor executor) {
        if (executor instanceof ExecutorService) {
            if (((ExecutorService) executor).isTerminated()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 线程池优雅关闭
     * Use the shutdown pattern from:
     * https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ExecutorService.html
     *
     * @param executor the Executor to shutdown
     * @param timeout  the timeout in milliseconds before termination
     */
    public static void gracefulShutdown(Executor executor, int timeout, String executorName) {
        if (!(executor instanceof ExecutorService) || isTerminated(executor)) {
            return;
        }
        final ExecutorService es = (ExecutorService) executor;
        try {
            // Disable new tasks from being submitted
            es.shutdown();
            logger.info(">>>>>>>>>>>线程池" + executorName + "正在关闭中…………");
        } catch (SecurityException | NullPointerException ex2) {
            return;
        }
        try {
            // Wait a while for existing tasks to terminate
            if (!es.awaitTermination(timeout, TimeUnit.MILLISECONDS)) {
                logger.warn(">>>>>>>>>>>线程池" + executorName + "等待" + timeout + "ms后仍然运行中，进行强制shutdownNow");
                es.shutdownNow();
            }
        } catch (InterruptedException ex) {
            logger.error(">>>>>>>>>>>线程池" + executorName + "强制shutdownNow出现异常{}", ex);
            es.shutdownNow();
            Thread.currentThread().interrupt();
        }
        if (!isTerminated(es)) {
            logger.error(">>>>>>>>>>>线程池" + executorName + "强制shutdownNow失败，新建线程开始关闭.");
            newThreadToCloseExecutor(es, executorName);
        }
    }

    public static void shutdownNow(Executor executor, final int timeout, final String executorName) {
        if (!(executor instanceof ExecutorService) || isTerminated(executor)) {
            return;
        }
        final ExecutorService es = (ExecutorService) executor;
        try {
            es.shutdownNow();
        } catch (SecurityException | NullPointerException ex2) {
            return;
        }
        try {
            es.awaitTermination(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        if (!isTerminated(es)) {
            newThreadToCloseExecutor(es, executorName);
        }
    }

    private static void newThreadToCloseExecutor(final ExecutorService es, String executorName) {
        if (!isTerminated(es)) {
            SHUTDOWN_EXECUTOR.execute(() -> {
                try {
                    for (int i = 0; i < 1000; i++) {
                        logger.info(">>>>>>>>>>>线程池" + executorName + "正在进行循环强制关闭，尝试第" + i + "次");
                        es.shutdownNow();
                        if (es.awaitTermination(10, TimeUnit.MILLISECONDS)) {
                            logger.info(">>>>>>>>>>>线程池" + executorName + "正在进行循环强制关闭，尝试第" + i + "次成功");
                            break;
                        }
                    }
                } catch (InterruptedException ex) {
                    logger.error(">>>>>>>>>>>线程池" + executorName + "正在进行循环强制关闭，出现异常:{}", ex);
                    Thread.currentThread().interrupt();
                } catch (Throwable e) {
                    logger.warn(e.getMessage(), e);
                }
            });
        }
    }
}
