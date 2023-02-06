package com.xxl.job.admin.core.thread;

import com.xxl.job.admin.core.ExecutorManagerClientRepository;
import com.xxl.job.admin.core.model.XxlJobGroup;
import com.xxl.job.admin.core.model.XxlJobRegistry;
import com.xxl.job.admin.dao.XxlJobGroupDao;
import com.xxl.job.admin.dao.XxlJobRegistryDao;
import com.xxl.job.common.enums.RegistryConstants;
import com.xxl.job.common.model.ReturnT;
import com.xxl.job.common.utils.DateUtil;
import com.xxl.job.common.utils.NamedThreadFactory;
import com.xxl.job.common.utils.ThreadPoolExecutorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.*;
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

    private final ExecutorManagerClientRepository executorManagerClientRepository;

    private final ThreadPoolExecutor heartBeatCheckThreadPool;

    private final ScheduledThreadPoolExecutor heartBeatThreadPool;

    private static final String ADMIN_HEARTBEAT_CHECK = "admin-heartBeat-check";

    private static final String ADMIN_REGISTRY_OR_REMOVE = "admin-registryOrRemove";

    private static final ConcurrentHashMap<String, List<XxlJobRegistry>> ZOMBIE_REGISTRY_MAP = new ConcurrentHashMap<>();

    public JobRegistryHelper(XxlJobGroupDao jobGroupDao, XxlJobRegistryDao jobRegistryDao, ExecutorManagerClientRepository executorManagerClientRepository) {
        this.jobGroupDao = jobGroupDao;
        this.jobRegistryDao = jobRegistryDao;
        this.executorManagerClientRepository = executorManagerClientRepository;
        // for registry or remove TODO 请求Netty服务器，保持心跳
        this.heartBeatCheckThreadPool = new ThreadPoolExecutor(
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
        // 心跳检测
        heartBeatThreadPool.scheduleAtFixedRate(() -> heartBeatSchedule(), 0, RegistryConstants.BEAT_TIMEOUT, TimeUnit.SECONDS);

        // 僵尸节点检测
        heartBeatThreadPool.scheduleAtFixedRate(() -> zombieSchedule(), RegistryConstants.BEAT_TIMEOUT, RegistryConstants.BEAT_TIMEOUT, TimeUnit.SECONDS);
    }

    private void zombieSchedule() {
        Date current = new Date();
        for (Map.Entry<String, List<XxlJobRegistry>> entry : ZOMBIE_REGISTRY_MAP.entrySet()) {
            XxlJobGroup xxlJobGroup = jobGroupDao.selectByAppName(entry.getKey());
            if (Objects.isNull(xxlJobGroup)) {
                // 如果xxlJobGroup不存在，删除ZOMBIE_REGISTRY_MAP中的值，不执行后续的流程
                ZOMBIE_REGISTRY_MAP.remove(entry.getKey());
                continue;
            }
            // 开始心跳检查
            List<XxlJobRegistry> xxlJobRegistryList = entry.getValue();
            // 心跳成功的数据
            List<XxlJobRegistry> beatSuccessRegistry = new ArrayList<>();
            List<String> addressList = new ArrayList<>();
            Iterator<XxlJobRegistry> iterator = xxlJobRegistryList.iterator();
            while (iterator.hasNext()) {
                XxlJobRegistry each = iterator.next();
                // 如果最后一次的心跳时间距离当前时间已经超过两个ZOMBIE_DEAD_TIMEOUT，删除当前节点
                Boolean exceed = DateUtil.exceed(each.getUpdateTime(), current, RegistryConstants.ZOMBIE_DEAD_TIMEOUT * 1000);
                if (exceed) {
                    iterator.remove();
                    // 删除客户端
                    executorManagerClientRepository.remove(each.getRegistryValue());
                    continue;
                }
                ReturnT<String> beatResult = executorManagerClientRepository.getExecutorManagerClient(each.getRegistryValue()).beat();
                if (beatResult.getCode() == ReturnT.SUCCESS_CODE) {
                    // 心跳成功
                    each.setUpdateTime(current);
                    beatSuccessRegistry.add(each);
                    addressList.add(each.getRegistryValue());
                    // 从iterator中删除
                    iterator.remove();
                }
            }
            if (CollectionUtils.isEmpty(xxlJobRegistryList)) {
                ZOMBIE_REGISTRY_MAP.remove(entry.getKey());
            }
            if (!CollectionUtils.isEmpty(beatSuccessRegistry)) {
                jobRegistryDao.saveBatch(beatSuccessRegistry);
                // fresh group address
                String addressString = addressList.stream().collect(Collectors.joining(","));
                if (StringUtils.hasText(xxlJobGroup.getAddressList())) {
                    xxlJobGroup.setAddressList(xxlJobGroup.getAddressList() + "," + addressString);
                } else {
                    xxlJobGroup.setAddressList(addressString);
                }
                xxlJobGroup.setUpdateTime(current);
                jobGroupDao.update(xxlJobGroup);
            }
        }
    }

    private void heartBeatSchedule() {
        try {
            // remove dead address (admin/executor)
            // 删除最后活跃时间大于90秒的节点
            List<XxlJobRegistry> deadRegistry = jobRegistryDao.findDead(RegistryConstants.DEAD_TIMEOUT, new Date());
            List<Integer> deadIds = new ArrayList<>();
            for (XxlJobRegistry each : deadRegistry) {
                deadIds.add(each.getId());
                putToZombie(each);
            }
            jobRegistryDao.removeDead(deadIds);

            // fresh online address (admin/executor)
            // 存储所有仍存活的节点
            ConcurrentHashMap<String, List<String>> appAddressMap = new ConcurrentHashMap<>();
            // 心跳成功的数据
            List<Integer> beatSuccessRegistryIds = new CopyOnWriteArrayList<>();
            // 查询所有存活的节点
            List<XxlJobRegistry> list = jobRegistryDao.findAll(RegistryConstants.DEAD_TIMEOUT, new Date());
            CompletableFuture[] completableFutures = list.stream().map(each -> CompletableFuture.runAsync(() -> {
                String appName = each.getRegistryKey();
                ReturnT<String> beatResult = executorManagerClientRepository.getExecutorManagerClient(each.getRegistryValue()).beat();
                if (beatResult.getCode() == ReturnT.SUCCESS_CODE) {
                    // 心跳成功
                    List<String> registryList = appAddressMap.computeIfAbsent(appName, k -> new ArrayList<>());
                    if (!registryList.contains(each.getRegistryValue())) {
                        registryList.add(each.getRegistryValue());
                    }
                    appAddressMap.put(appName, registryList);
                    beatSuccessRegistryIds.add(each.getId());
                } else {
                    // 心跳失败，加入到ZOMBIE_REGISTRY_MAP
                    putToZombie(each);
                }
            }, heartBeatCheckThreadPool)).toArray(array -> new CompletableFuture[list.size()]);
            CompletableFuture.allOf(completableFutures);
            // 更新所有心跳成功的节点
            Date current = new Date();
            if (!CollectionUtils.isEmpty(beatSuccessRegistryIds)) {
                jobRegistryDao.beatUpdate(beatSuccessRegistryIds, current);
            }
            // fresh group address
            for (Map.Entry<String, List<String>> entry : appAddressMap.entrySet()) {
                XxlJobGroup xxlJobGroup = jobGroupDao.selectByAppName(entry.getKey());
                if (Objects.isNull(xxlJobGroup)) {
                    continue;
                }
                List<String> addressList = entry.getValue();
                xxlJobGroup.setAddressList(addressList.stream().collect(Collectors.joining(",")));
                xxlJobGroup.setUpdateTime(current);
                jobGroupDao.update(xxlJobGroup);
            }
        } catch (Exception e) {
            logger.error("Admin心跳检测异常", e);
        }
    }

    private void putToZombie(XxlJobRegistry registry) {
        List<XxlJobRegistry> zombieListByAppName = ZOMBIE_REGISTRY_MAP.computeIfAbsent(registry.getRegistryKey(), ele -> new ArrayList<>());
        if (!zombieListByAppName.contains(registry.getRegistryValue())) {
            zombieListByAppName.add(registry);
        }
        ZOMBIE_REGISTRY_MAP.put(registry.getRegistryKey(), zombieListByAppName);
    }

    public void toStop() {
        // stop registryOrRemoveThreadPool
        ThreadPoolExecutorUtil.gracefulShutdown(heartBeatCheckThreadPool, 1000, ADMIN_REGISTRY_OR_REMOVE);

        // stop heartBeatThreadPool
        ThreadPoolExecutorUtil.gracefulShutdown(heartBeatThreadPool, 3000, ADMIN_HEARTBEAT_CHECK);
    }
}
