package org.openwes.mq.redis.listener;

import org.openwes.common.utils.user.UserContext;
import org.openwes.common.utils.utils.RedisUtils;
import org.openwes.mq.MqWrapper;
import org.openwes.mq.redis.RedisListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisConsumeProcessor implements BeanPostProcessor {

    private final RedisUtils redisUtils;

    @Value("${mq.type:redis}")
    private String mqType;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        if (!StringUtils.equals("redis", mqType)) {
            return bean;
        }

        Method[] methods = ReflectionUtils.getAllDeclaredMethods(bean.getClass());
        for (Method method : methods) {
            RedisListener mqConsumer = AnnotationUtils.findAnnotation(method, RedisListener.class);
            if (null != mqConsumer && StringUtils.isNotEmpty(mqConsumer.topic())) {
                redisUtils.listen(mqConsumer.topic(), MqWrapper.class, (channel, mqWrapper) -> {
                    log.debug("get message: {} from topic: {}", mqWrapper, channel);
                    try {
                        if (mqWrapper != null) {
                            UserContext.setAccount(mqWrapper.getUsername());
                            method.invoke(bean, String.valueOf(channel), mqWrapper.getMessage());
                        } else {
                            log.error("redis topic: {} message is null error: ", channel);
                        }
                    } catch (Exception e) {
                        log.error("redis topic: {} consume error: ", channel, e);
                    } finally {
                        UserContext.removeUser();
                    }
                });
            }
        }
        return bean;
    }

}
