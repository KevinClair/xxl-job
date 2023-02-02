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

    private final ThreadPoolExecutor registryOrRemoveThreadPool;

    private final ScheduledThreadPoolExecutor heartBeatThreadPool;

    private static final String ADMIN_HEARTBEAT_CHECK = "admin-heartBeat-check";

    private static final String ADMIN_REGISTRY_OR_REMOVE = "admin-registryOrRemove";

    private static final ConcurrentHashMap<String, List<XxlJobRegistry>> ZOMBIE_REGISTRY_MAP = new ConcurrentHashMap<>();

    public JobRegistryHelper(XxlJobGroupDao jobGroupDao, XxlJobRegistryDao jobRegistryDao, ExecutorManagerClientRepository executorManagerClientRepository) {
        this.jobGroupDao = jobGroupDao;
        this.jobRegistryDao = jobRegistryDao;
        this.executorManagerClientRepository = executorManagerClientRepository;
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
        // 心跳检测
        heartBeatThreadPool.scheduleAtFixedRate(() -> {
            try {
                // auto registry group
//                List<XxlJobGroup> groupList = jobGroupDao.findByAddressType(0);
//                if (!CollectionUtils.isEmpty(groupList)) {

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
                HashMap<String, List<String>> appAddressMap = new HashMap<>();
                // 心跳成功的数据
                List<Integer> beatSuccessRegistryIds = new ArrayList<>();
                // 查询所有存活的节点
                List<XxlJobRegistry> list = jobRegistryDao.findAll(RegistryConstants.DEAD_TIMEOUT, new Date());
                list.stream()
                        .forEach(each -> {
                            // TODO 改为多线程
                            String appName = each.getRegistryKey();
                            ReturnT<String> beatResult = executorManagerClientRepository.getExecutorManagerClient(each.getRegistryValue()).beat();
                            if (beatResult.getCode() == ReturnT.SUCCESS_CODE) {
                                // 心跳成功
                                List<String> registryList = appAddressMap.getOrDefault(appName, new ArrayList<>());
                                if (!registryList.contains(each.getRegistryValue())) {
                                    registryList.add(each.getRegistryValue());
                                }
                                appAddressMap.put(appName, registryList);
                                beatSuccessRegistryIds.add(each.getId());
                            } else {
                                // 心跳失败，加入到ZOMBIE_REGISTRY_MAP
                                putToZombie(each);
                            }
                        });

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
                    // TODO 这里为啥需要排序
                    List<String> addressList = entry.getValue();
                    Collections.sort(addressList);
                    xxlJobGroup.setAddressList(addressList.stream().collect(Collectors.joining(",")));
                    xxlJobGroup.setUpdateTime(current);
                    jobGroupDao.update(xxlJobGroup);
                }
            } catch (Exception e) {
                logger.error("Admin心跳检测异常", e);
            }
        }, 0, RegistryConstants.BEAT_TIMEOUT, TimeUnit.SECONDS);

        // 僵尸节点检测
        heartBeatThreadPool.scheduleAtFixedRate(() -> {
            Date current = new Date();
            for (Map.Entry<String, List<XxlJobRegistry>> entry : ZOMBIE_REGISTRY_MAP.entrySet()) {
                // 开始心跳检查
                List<XxlJobRegistry> xxlJobRegistryList = entry.getValue();
                // 存储所有心跳成功的僵尸节点
                HashMap<String, List<String>> appAddressMap = new HashMap<>();
                // 心跳成功的数据
                List<Integer> beatSuccessRegistryIds = new ArrayList<>();
                Iterator<XxlJobRegistry> iterator = xxlJobRegistryList.iterator();
                while (iterator.hasNext()) {
                    XxlJobRegistry each = iterator.next();
                    // 如果最后一次的心跳时间距离当前时间已经超过两个ZOMBIE_DEAD_TIMEOUT，删除当前节点
                    Boolean exceed = DateUtil.exceed(each.getUpdateTime(), current, RegistryConstants.ZOMBIE_DEAD_TIMEOUT * 1000);
                    if (exceed) {
                        xxlJobRegistryList.remove(each);
                        // 删除客户端
                        executorManagerClientRepository.remove(each.getRegistryValue());
                        continue;
                    }

                    ReturnT<String> beatResult = executorManagerClientRepository.getExecutorManagerClient(each.getRegistryValue()).beat();
                    if (beatResult.getCode() == ReturnT.SUCCESS_CODE) {
                        // 心跳成功
                        List<String> registryList = appAddressMap.getOrDefault(entry.getKey(), new ArrayList<>());
                        if (!registryList.contains(each.getRegistryValue())) {
                            registryList.add(each.getRegistryValue());
                        }
                        appAddressMap.put(entry.getKey(), registryList);
                        beatSuccessRegistryIds.add(each.getId());
                    }
                }
                if (!CollectionUtils.isEmpty(beatSuccessRegistryIds)) {
                    jobRegistryDao.beatUpdate(beatSuccessRegistryIds, current);
                }
                // fresh group address
                for (Map.Entry<String, List<String>> entryMap : appAddressMap.entrySet()) {
                    // TODO 这里为啥需要排序 这里理论上应该按照最后一次心跳时间排序，把最近的一次心跳的address放在最前面，这样确保负载均衡时的成功几率
                    List<String> addressList = entryMap.getValue();
                    // TODO addressList可能是空集合吗？
                    Collections.sort(addressList);
                    String addressString = addressList.stream().collect(Collectors.joining(","));
                    XxlJobGroup xxlJobGroup = jobGroupDao.selectByAppName(entryMap.getKey());
                    if (Objects.isNull(xxlJobGroup)) {
                        // 如果xxlJobGroup不存在，跳过
                        continue;
                    }
                    if (StringUtils.hasText(xxlJobGroup.getAddressList())) {
                        xxlJobGroup.setAddressList(xxlJobGroup.getAddressList() + "," + addressString);
                    } else {
                        xxlJobGroup.setAddressList(addressString);
                    }
                    xxlJobGroup.setUpdateTime(current);
                    jobGroupDao.update(xxlJobGroup);
                }

            }
        }, RegistryConstants.BEAT_TIMEOUT, RegistryConstants.BEAT_TIMEOUT, TimeUnit.SECONDS);
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
        ThreadPoolExecutorUtil.gracefulShutdown(registryOrRemoveThreadPool, 1000, ADMIN_REGISTRY_OR_REMOVE);

        // stop heartBeatThreadPool
        ThreadPoolExecutorUtil.gracefulShutdown(heartBeatThreadPool, 3000, ADMIN_HEARTBEAT_CHECK);
    }
}
