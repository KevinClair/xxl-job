package com.xxl.job.core.handler;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.core.handler.impl.MethodJobHandler;

/**
 * JobHandler管理工厂
 */
public class JobHandlerRepository implements ApplicationContextAware, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(JobHandlerRepository.class);

    private ApplicationContext applicationContext;

    private static ConcurrentMap<String, IJobHandler> jobHandlerRepository = new ConcurrentHashMap<>();

    /**
     * init job handler.
     */
    public void initJobHandlerMethodRepository() {
        // init job handler from method
        String[] beanDefinitionNames = applicationContext.getBeanDefinitionNames();
        for (String beanDefinitionName : beanDefinitionNames) {
            Lazy onBean = applicationContext.findAnnotationOnBean(beanDefinitionName, Lazy.class);
            if (Objects.nonNull(onBean)) {
                logger.debug("xxl-job annotation scan, skip @Lazy Bean:{}", beanDefinitionName);
                continue;
            }
            // get bean
            Object bean = applicationContext.getBean(beanDefinitionName);
            // filter method
            Map<Method, XxlJob> annotatedMethods = null;   // referred to ：org.springframework.context.event.EventListenerMethodProcessor.processBean
            try {
                annotatedMethods = MethodIntrospector.selectMethods(bean.getClass(),
                    (MethodIntrospector.MetadataLookup<XxlJob>) method -> AnnotatedElementUtils.findMergedAnnotation(method, XxlJob.class));
            } catch (Throwable ex) {
                logger.error("xxl-job method-jobHandler resolve error for bean[" + beanDefinitionName + "].", ex);
            }
            if (CollectionUtils.isEmpty(annotatedMethods)) {
                continue;
            }

            // generate and regist method job handler
            for (Map.Entry<Method, XxlJob> methodXxlJobEntry : annotatedMethods.entrySet()) {
                Method executeMethod = methodXxlJobEntry.getKey();
                XxlJob xxlJob = methodXxlJobEntry.getValue();
                // registry
                registerJobHandler(xxlJob, bean, executeMethod);
            }

        }
    }

    /**
     * register job handler.
     *
     * @param xxlJob        annotation.{@link XxlJob}
     * @param bean          current bean object.
     * @param executeMethod current method.
     */
    private void registerJobHandler(XxlJob xxlJob, Object bean, Method executeMethod) {
        if (Objects.isNull(xxlJob)) {
            return;
        }

        String name = xxlJob.value();
        //make and simplify the variables since they'll be called several times later
        Class<?> clazz = bean.getClass();
        String methodName = executeMethod.getName();
        if (!StringUtils.hasText(name)) {
            throw new RuntimeException("xxl-job method-jobHandler name invalid, for[" + clazz + "#" + methodName + "] .");
        }
        if (Objects.nonNull(loadJobHandler(name))) {
            throw new RuntimeException("xxl-job jobHandler[" + name + "] naming conflicts.");
        }
        executeMethod.setAccessible(true);
        // init and destroy
        Method initMethod = null;
        Method destroyMethod = null;

        if (xxlJob.init().trim().length() > 0) {
            try {
                initMethod = clazz.getDeclaredMethod(xxlJob.init());
                initMethod.setAccessible(true);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("xxl-job method-jobHandler initMethod invalid, for[" + clazz + "#" + methodName + "] .");
            }
        }
        if (xxlJob.destroy().trim().length() > 0) {
            try {
                destroyMethod = clazz.getDeclaredMethod(xxlJob.destroy());
                destroyMethod.setAccessible(true);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("xxl-job method-jobHandler destroyMethod invalid, for[" + clazz + "#" + methodName + "] .");
            }
        }

        // registry jobHandler,put into hash map.
        registerJobHandler(name, new MethodJobHandler(bean, executeMethod, initMethod, destroyMethod));
    }

    /**
     * get JobHandler by job's name.
     *
     * @param name
     * @return
     */
    public IJobHandler loadJobHandler(String name){
        return jobHandlerRepository.get(name);
    }

    public IJobHandler registerJobHandler(String name, IJobHandler jobHandler){
        logger.info(">>>>>>>>>>> xxl-job register jobHandler success, name:{}, jobHandler:{}", name, jobHandler);
        return jobHandlerRepository.put(name, jobHandler);
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void destroy() throws Exception {
        jobHandlerRepository.clear();
    }
}
