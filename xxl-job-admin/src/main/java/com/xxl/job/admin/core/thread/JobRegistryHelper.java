package com.xxl.job.admin.core.thread;

import com.xxl.job.admin.core.model.XxlJobGroup;
import com.xxl.job.admin.core.model.XxlJobRegistry;
import com.xxl.job.admin.dao.XxlJobGroupDao;
import com.xxl.job.admin.dao.XxlJobRegistryDao;
import com.xxl.job.common.enums.RegistryConstants;
import com.xxl.job.common.model.RegistryParam;
import com.xxl.job.common.model.ReturnT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * job registry instance
 * @author xuxueli 2016-10-02 19:10:24
 */
@Component
public class JobRegistryHelper {
    private static Logger logger = LoggerFactory.getLogger(JobRegistryHelper.class);

    private final XxlJobGroupDao jobGroupDao;

    private final XxlJobRegistryDao jobRegistryDao;

    private final ThreadPoolExecutor registryOrRemoveThreadPool;

    private Thread registryMonitorThread;

    public JobRegistryHelper(XxlJobGroupDao jobGroupDao, XxlJobRegistryDao jobRegistryDao) {
        this.jobGroupDao = jobGroupDao;
        this.jobRegistryDao = jobRegistryDao;
        // for registry or remove TODO 请求Netty服务器，保持心跳
        this.registryOrRemoveThreadPool = new ThreadPoolExecutor(
                2,
                10,
                30L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(2000),
                r -> new Thread(r, "xxl-job, admin JobRegistryMonitorHelper-registryOrRemoveThreadPool-" + r.hashCode()),
                (r, executor) -> {
                    r.run();
                    logger.warn(">>>>>>>>>>> xxl-job, registry or remove too fast, match threadpool rejected handler(run now).");
                });


    }

    private volatile boolean toStop = false;

    public void start() {
        // for monitor
        // TODO 改为定时任务线程池
        this.registryMonitorThread = new Thread(() -> {
            while (!toStop) {
                try {
                    // auto registry group
                    List<XxlJobGroup> groupList = jobGroupDao.findByAddressType(0);
                    if (groupList != null && !groupList.isEmpty()) {

                        // remove dead address (admin/executor)
                        // 删除最后活跃时间大于90秒的节点
                        List<Integer> ids = jobRegistryDao.findDead(RegistryConstants.DEAD_TIMEOUT, new Date());
                        if (ids != null && ids.size() > 0) {
                            jobRegistryDao.removeDead(ids);
                        }

                        // fresh online address (admin/executor)
                        HashMap<String, List<String>> appAddressMap = new HashMap<String, List<String>>();
                        // 查询所有存活的节点
                        List<XxlJobRegistry> list = jobRegistryDao.findAll(RegistryConstants.DEAD_TIMEOUT, new Date());
                        for (XxlJobRegistry item : list) {
                            if (RegistryConstants.RegistryType.EXECUTOR.name().equals(item.getRegistryGroup())) {
                                String appname = item.getRegistryKey();
                                List<String> registryList = appAddressMap.get(appname);
                                if (registryList == null) {
                                    registryList = new ArrayList<String>();
                                }

                                if (!registryList.contains(item.getRegistryValue())) {
                                    registryList.add(item.getRegistryValue());
                                }
                                appAddressMap.put(appname, registryList);
                            }
                        }

                        // fresh group address
                        // TODO 代码逻辑完善
                        for (XxlJobGroup group : groupList) {
                            List<String> registryList = appAddressMap.get(group.getAppname());
                            String addressListStr = null;
                            if (registryList != null && !registryList.isEmpty()) {
                                Collections.sort(registryList);
                                StringBuilder addressListSB = new StringBuilder();
                                for (String item : registryList) {
                                    addressListSB.append(item).append(",");
                                }
                                addressListStr = addressListSB.toString();
                                addressListStr = addressListStr.substring(0, addressListStr.length() - 1);
                            }
                            group.setAddressList(addressListStr);
                            group.setUpdateTime(new Date());

                            jobGroupDao.update(group);
                        }
                    }
                } catch (Exception e) {
                    if (!toStop) {
                        logger.error(">>>>>>>>>>> xxl-job, job registry monitor thread error:{}", e);
                    }
                }
                try {
                    TimeUnit.SECONDS.sleep(RegistryConstants.BEAT_TIMEOUT);
                } catch (InterruptedException e) {
                    if (!toStop) {
                        logger.error(">>>>>>>>>>> xxl-job, job registry monitor thread error:{}", e);
                    }
                }
            }
            logger.info(">>>>>>>>>>> xxl-job, job registry monitor thread stop");
        });
        registryMonitorThread.setDaemon(true);
        registryMonitorThread.setName("xxl-job, admin JobRegistryMonitorHelper-registryMonitorThread");
        registryMonitorThread.start();
    }

    public void toStop() {
        toStop = true;

        // stop registryOrRemoveThreadPool
        registryOrRemoveThreadPool.shutdownNow();

        // stop monitir (interrupt and wait)
        registryMonitorThread.interrupt();
        try {
            registryMonitorThread.join();
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }


    // ---------------------- helper ----------------------

    public ReturnT<String> registry(RegistryParam registryParam) {

        // async execute
        registryOrRemoveThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                int ret = jobRegistryDao.registryUpdate(registryParam.getRegistryGroup(), registryParam.getRegistryKey(), registryParam.getRegistryValue(), new Date());
                if (ret < 1) {
                    jobRegistryDao.registrySave(registryParam.getRegistryGroup(), registryParam.getRegistryKey(), registryParam.getRegistryValue(), new Date());

                    // fresh
                    freshGroupRegistryInfo(registryParam);
                }
            }
        });

        return ReturnT.SUCCESS;
    }

    public ReturnT<String> registryRemove(RegistryParam registryParam) {

        // async execute
        registryOrRemoveThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                int ret = jobRegistryDao.registryDelete(registryParam.getRegistryGroup(), registryParam.getRegistryKey(), registryParam.getRegistryValue());
                if (ret > 0) {
                    // fresh
                    freshGroupRegistryInfo(registryParam);
                }
            }
        });

        return ReturnT.SUCCESS;
    }

    private void freshGroupRegistryInfo(RegistryParam registryParam) {
        // Under consideration, prevent affecting core tables
    }


}
