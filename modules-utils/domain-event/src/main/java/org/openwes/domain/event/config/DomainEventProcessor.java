package org.openwes.domain.event.config;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

@Component
public class DomainEventProcessor implements BeanPostProcessor {

    @Resource(name = "asyncEventBus")
    private EventBus asyncEventBus;

    @Resource(name = "syncEventBus")
    private EventBus syncEventBus;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        Method[] methods = ReflectionUtils.getAllDeclaredMethods(bean.getClass());
        for (Method method : methods) {
            if (method.isAnnotationPresent(Subscribe.class)) {
                asyncEventBus.register(bean);
                syncEventBus.register(bean);
            }
        }
        return bean;
    }
}
