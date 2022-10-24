package com.xxl.job.admin.core.route;

import com.xxl.job.admin.core.route.strategy.ExecutorRouteBusyover;
import com.xxl.job.admin.core.route.strategy.ExecutorRouteFailover;
import com.xxl.job.admin.core.util.I18nUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.stereotype.Component;

/**
 * 执行器路由工厂Bean注册器
 */
@Component
public class ExecutorRouterBeanDefinitionRegistry implements BeanDefinitionRegistryPostProcessor {


    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry beanDefinitionRegistry) throws BeansException {
        BeanDefinition executorBizRepository = beanDefinitionRegistry.getBeanDefinition("executorBizRepository");
        BeanDefinitionBuilder executorRouteBusyover = BeanDefinitionBuilder.genericBeanDefinition(ExecutorRouteBusyover.class);
        executorRouteBusyover.addConstructorArgValue(executorBizRepository);
        beanDefinitionRegistry.registerBeanDefinition(I18nUtil.getString("jobconf_route_busyover"), executorRouteBusyover.getBeanDefinition());


        BeanDefinitionBuilder executorRouteFailover = BeanDefinitionBuilder.genericBeanDefinition(ExecutorRouteFailover.class);
        executorRouteFailover.addConstructorArgValue(executorBizRepository);
        beanDefinitionRegistry.registerBeanDefinition(I18nUtil.getString("jobconf_route_failover"), executorRouteFailover.getBeanDefinition());
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {

    }
}
