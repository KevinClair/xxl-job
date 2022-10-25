package com.xxl.job.admin.core.route;

import com.xxl.job.admin.core.util.I18nUtil;

/**
 * Created by xuxueli on 17/3/10.
 */
public enum ExecutorRouteStrategyEnum {

    FIRST(I18nUtil.getString("jobconf_route_first"), "executorRouteFirst"),
    LAST(I18nUtil.getString("jobconf_route_last"), "executorRouteLast"),
    ROUND(I18nUtil.getString("jobconf_route_round"), "executorRouteRound"),
    RANDOM(I18nUtil.getString("jobconf_route_random"), "executorRouteRandom"),
    CONSISTENT_HASH(I18nUtil.getString("jobconf_route_consistenthash"), "executorRouteConsistentHash"),
    LEAST_FREQUENTLY_USED(I18nUtil.getString("jobconf_route_lfu"), "executorRouteLFU"),
    LEAST_RECENTLY_USED(I18nUtil.getString("jobconf_route_lru"), "executorRouteLRU"),
    FAILOVER(I18nUtil.getString("jobconf_route_failover"), "executorRouteFailover"),
    BUSYOVER(I18nUtil.getString("jobconf_route_busyover"), "executorRouteBusyover"),
    SHARDING_BROADCAST(I18nUtil.getString("jobconf_route_shard"), null);

    ExecutorRouteStrategyEnum(String title, String routerBeanName) {
        this.title = title;
        this.routerBeanName = routerBeanName;
    }

    /**
     * 执行器路由规则名
     */
    private String title;

    /**
     * 对应的路由规则Bean的名称
     */
    private String routerBeanName;

    public String getTitle() {
        return title;
    }
    public String getRouterBeanName() {
        return routerBeanName;
    }

    public static ExecutorRouteStrategyEnum match(String name, ExecutorRouteStrategyEnum defaultItem){
        if (name != null) {
            for (ExecutorRouteStrategyEnum item: ExecutorRouteStrategyEnum.values()) {
                if (item.name().equals(name)) {
                    return item;
                }
            }
        }
        return defaultItem;
    }

}
