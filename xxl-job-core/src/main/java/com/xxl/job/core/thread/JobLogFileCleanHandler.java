package com.xxl.job.core.thread;

import com.xxl.job.core.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * job file clean thread
 *
 * @author xuxueli 2017-12-29 16:23:43
 */
public class JobLogFileCleanHandler {
    private static Logger logger = LoggerFactory.getLogger(JobLogFileCleanHandler.class);

    private final ScheduledThreadPoolExecutor threadPoolExecutor;

    public JobLogFileCleanHandler(final long logRetentionDays, final String logPath) {
        threadPoolExecutor = new ScheduledThreadPoolExecutor(1, r -> new Thread(r, "xxl-job, Expire log clear thread."));
        // 计算下一天的零点整和当前时间的差值时间
        Calendar nextDayZeroTime = Calendar.getInstance();
        nextDayZeroTime.set(Calendar.HOUR_OF_DAY,0);
        nextDayZeroTime.set(Calendar.MINUTE,0);
        nextDayZeroTime.set(Calendar.SECOND,0);
        nextDayZeroTime.set(Calendar.MILLISECOND,0);
        nextDayZeroTime.add(Calendar.DATE,1);
        threadPoolExecutor.scheduleAtFixedRate(() -> {
            File[] childDirs = new File(logPath).listFiles();
            if (childDirs!=null && childDirs.length>0) {
                Date todayDate = new Date();

                for (File childFile: childDirs) {
                    // valid
                    if (!childFile.isDirectory()) {
                        continue;
                    }
                    if (!childFile.getName().contains("-")) {
                        continue;
                    }

                    // file create date
                    Date logFileCreateDate = null;
                    try {
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        logFileCreateDate = simpleDateFormat.parse(childFile.getName());
                    } catch (ParseException e) {
                        logger.error("Clear log error.", e);
                    }
                    if (logFileCreateDate == null) {
                        continue;
                    }

                    if ((todayDate.getTime()-logFileCreateDate.getTime()) >= logRetentionDays * (24 * 60 * 60 * 1000) ) {
                        FileUtil.deleteRecursively(childFile);
                    }
                }
            }
        }, nextDayZeroTime.getTimeInMillis()-System.currentTimeMillis(), 24 * 60 * 60 * 1000, TimeUnit.MILLISECONDS);
    }


    /**
     * shutdown thread pool.
     */
    public void toStop() {
        threadPoolExecutor.shutdown();
    }

}
