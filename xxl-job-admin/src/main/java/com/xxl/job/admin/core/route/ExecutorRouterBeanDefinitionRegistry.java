package com.xxl.job.admin.core.route;

import com.xxl.job.admin.core.route.strategy.*;
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
        registerExecutorRouteBusyover(beanDefinitionRegistry, executorBizRepository);
        registerExecutorRouteFailover(beanDefinitionRegistry, executorBizRepository);
        registerExecutorRouteLast(beanDefinitionRegistry);
        registerExecutorRouteRound(beanDefinitionRegistry);
        registerExecutorRouteRandom(beanDefinitionRegistry);
        registerExecutorRouteConsistentHash(beanDefinitionRegistry);
        registerExecutorRouteLFU(beanDefinitionRegistry);
        registerExecutorRouteLRU(beanDefinitionRegistry);
        registerExecutorRouteFirst(beanDefinitionRegistry);
    }

    private void registerExecutorRouteBusyover(BeanDefinitionRegistry beanDefinitionRegistry, BeanDefinition executorBizRepository) {
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(ExecutorRouteBusyover.class);
        beanDefinitionBuilder.addConstructorArgValue(executorBizRepository);
        beanDefinitionRegistry.registerBeanDefinition("executorRouteBusyover", beanDefinitionBuilder.getBeanDefinition());
    }

    private void registerExecutorRouteFailover(BeanDefinitionRegistry beanDefinitionRegistry, BeanDefinition executorBizRepository) {
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(ExecutorRouteFailover.class);
        beanDefinitionBuilder.addConstructorArgValue(executorBizRepository);
        beanDefinitionRegistry.registerBeanDefinition("executorRouteFailover", beanDefinitionBuilder.getBeanDefinition());
    }

    private void registerExecutorRouteLast(BeanDefinitionRegistry beanDefinitionRegistry) {
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(ExecutorRouteLast.class);
        beanDefinitionRegistry.registerBeanDefinition("executorRouteLast", beanDefinitionBuilder.getBeanDefinition());
    }

    private void registerExecutorRouteRound(BeanDefinitionRegistry beanDefinitionRegistry) {
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(ExecutorRouteRound.class);
        beanDefinitionRegistry.registerBeanDefinition("executorRouteRound", beanDefinitionBuilder.getBeanDefinition());
    }

    private void registerExecutorRouteRandom(BeanDefinitionRegistry beanDefinitionRegistry) {
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(ExecutorRouteRandom.class);
        beanDefinitionRegistry.registerBeanDefinition("executorRouteRandom", beanDefinitionBuilder.getBeanDefinition());
    }

    private void registerExecutorRouteConsistentHash(BeanDefinitionRegistry beanDefinitionRegistry) {
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(ExecutorRouteConsistentHash.class);
        beanDefinitionRegistry.registerBeanDefinition("executorRouteConsistentHash", beanDefinitionBuilder.getBeanDefinition());
    }

    private void registerExecutorRouteLFU(BeanDefinitionRegistry beanDefinitionRegistry) {
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(ExecutorRouteLFU.class);
        beanDefinitionRegistry.registerBeanDefinition("executorRouteLFU", beanDefinitionBuilder.getBeanDefinition());
    }

    private void registerExecutorRouteLRU(BeanDefinitionRegistry beanDefinitionRegistry) {
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(ExecutorRouteLRU.class);
        beanDefinitionRegistry.registerBeanDefinition("executorRouteLRU", beanDefinitionBuilder.getBeanDefinition());
    }

    private void registerExecutorRouteFirst(BeanDefinitionRegistry beanDefinitionRegistry) {
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(ExecutorRouteFirst.class);
        beanDefinitionRegistry.registerBeanDefinition("executorRouteFirst", beanDefinitionBuilder.getBeanDefinition());
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory configurableListableBeanFactory) throws BeansException {

    }
}
