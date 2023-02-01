package com.xxl.job.admin.core.thread;

import com.xxl.job.admin.core.model.XxlJobGroup;
import com.xxl.job.admin.core.model.XxlJobRegistry;
import com.xxl.job.admin.dao.XxlJobGroupDao;
import com.xxl.job.admin.dao.XxlJobRegistryDao;
import com.xxl.job.common.enums.RegistryConstants;
import com.xxl.job.common.model.RegistryParam;
import com.xxl.job.common.model.ReturnT;
import com.xxl.job.common.utils.NamedThreadFactory;
import com.xxl.job.common.utils.ThreadPoolExecutorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * job registry instance
 *
 * @author xuxueli 2016-10-02 19:10:24
 */
@Component
public class JobRegistryHelper {
    private static Logger logger = LoggerFactory.getLogger(JobRegistryHelper.class);

    private final XxlJobGroupDao jobGroupDao;

    private final XxlJobRegistryDao jobRegistryDao;

    private final ThreadPoolExecutor registryOrRemoveThreadPool;

    private final ScheduledThreadPoolExecutor heartBeatThreadPool;

    private static final String ADMIN_HEARTBEAT_CHECK = "admin-heartBeat-check";

    private static final String ADMIN_REGISTRY_OR_REMOVE = "admin-registryOrRemove";

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
                new NamedThreadFactory(ADMIN_REGISTRY_OR_REMOVE),
                (r, executor) -> {
                    r.run();
                    logger.warn(">>>>>>>>>>> xxl-job, registry or remove too fast, match threadpool rejected handler(run now).");
                });
        this.heartBeatThreadPool = new ScheduledThreadPoolExecutor(2, new NamedThreadFactory(ADMIN_HEARTBEAT_CHECK));
    }

    public void start() {
        // for monitor
        heartBeatThreadPool.scheduleAtFixedRate(() -> {
            try {
                // auto registry group
                List<XxlJobGroup> groupList = jobGroupDao.findByAddressType(0);
                if (!CollectionUtils.isEmpty(groupList)) {

                    // remove dead address (admin/executor)
                    // 删除最后活跃时间大于90秒的节点
                    List<Integer> ids = jobRegistryDao.findDead(RegistryConstants.DEAD_TIMEOUT, new Date());
                    if (!CollectionUtils.isEmpty(ids)) {
                        // TODO 移入僵尸列表
//                            jobRegistryDao.removeDead(ids);
                    }

                    // fresh online address (admin/executor)
                    // 存储所有自动注册的，目前仍存活的节点
                    HashMap<String, List<String>> appAddressMap = new HashMap<>();
                    // 查询所有存活的节点
                    List<XxlJobRegistry> list = jobRegistryDao.findAll(RegistryConstants.DEAD_TIMEOUT, new Date());
                    list.stream()
                            .filter(each -> RegistryConstants.RegistryType.EXECUTOR.name().equals(each.getRegistryGroup()))
                            .forEach(each -> {
                                String appName = each.getRegistryKey();
                                List<String> registryList = appAddressMap.getOrDefault(appName, new ArrayList<>());
                                if (!registryList.contains(each.getRegistryValue())) {
                                    registryList.add(each.getRegistryValue());
                                }
                                appAddressMap.put(appName, registryList);
                            });

                    // fresh group address
                    // TODO 代码逻辑完善 心跳检测
                    for (XxlJobGroup group : groupList) {
                        List<String> registryList = appAddressMap.get(group.getAppname());
                        String addressListStr = null;
                        if (CollectionUtils.isEmpty(registryList)) {
                            // TODO 这里为啥需要排序 这里理论上应该按照最后一次心跳时间排序，把最近的一次心跳的address放在最前面，这样确保负载均衡时的成功几率
                            Collections.sort(registryList);
                            addressListStr = registryList.stream().collect(Collectors.joining(","));
                        }
                        group.setAddressList(addressListStr);
                        group.setUpdateTime(new Date());
                        jobGroupDao.update(group);
                    }
                }
            } catch (Exception e) {

            }
        }, 0, RegistryConstants.BEAT_TIMEOUT, TimeUnit.SECONDS);
    }

    public void toStop() {
        // stop registryOrRemoveThreadPool
        ThreadPoolExecutorUtil.gracefulShutdown(registryOrRemoveThreadPool, 1000, ADMIN_REGISTRY_OR_REMOVE);

        // stop heartBeatThreadPool
        ThreadPoolExecutorUtil.gracefulShutdown(heartBeatThreadPool, 3000, ADMIN_HEARTBEAT_CHECK);
    }


    // ---------------------- helper ----------------------

    public ReturnT<String> registry(RegistryParam registryParam) {

        // async execute
        registryOrRemoveThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                // 应用端改为只在启动时注册，所以这里应该只会有新增
                jobRegistryDao.registrySave(registryParam.getRegistryGroup(), registryParam.getRegistryKey(), registryParam.getRegistryValue(), new Date());
                // fresh
                freshGroupRegistryInfo(registryParam);
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
