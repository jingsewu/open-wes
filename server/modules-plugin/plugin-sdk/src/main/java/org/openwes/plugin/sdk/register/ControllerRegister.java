package org.openwes.plugin.sdk.register;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.utils.FieldUtils;
import org.openwes.plugin.sdk.utils.PluginUtils;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.cglib.core.ReflectUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ControllerRegister extends AbstractRegister {

    private final Map<String, Set<RequestMappingInfo>> requestMappings = new ConcurrentHashMap<>();
    private final ApplicationContext applicationContext;

    @Override
    public void afterRegister(ApplicationContext pluginContent, PluginWrapper plugin) {
        try {
            Set<String> classNames = PluginUtils.readJarFile(plugin.getPluginPath().toAbsolutePath().toString());
            Set<RequestMappingInfo> pluginRequestMappings = new HashSet<>();
            for (String className : classNames) {
                Class<?> aClass = Class.forName(className, false, pluginContent.getClassLoader());
                if (PluginUtils.isController(aClass)) {
                    Object bean = pluginContent.getBean(aClass);
                    Set<RequestMappingInfo> requestMappingInfos = registerController(bean);
                    printRegisterSuccessController(plugin, requestMappingInfos);
                    pluginRequestMappings.addAll(requestMappingInfos);
                }
            }
            requestMappings.put(plugin.getPluginId(), pluginRequestMappings);
        } catch (Exception ex) {
            log.error("register controller error", ex);
        }
    }

    //TODO unRegister the controller not clear
    @Override
    public void unRegister(ApplicationContext pluginContent, PluginWrapper plugin) {
        Set<RequestMappingInfo> requestMappingInfoSet = requestMappings.get(plugin.getPluginId());
        if (requestMappingInfoSet != null) {
            requestMappingInfoSet.forEach(this::unRegisterController);
        }
        RequestMappingHandlerAdapter requestMappingHandlerAdapter = pluginContent.getBean(RequestMappingHandlerAdapter.class);

        Map<String, Object> controllerBeans = pluginContent.getBeansWithAnnotation(Controller.class);
        controllerBeans.forEach((name, bean) -> {
            ((Map<?, ?>) FieldUtils.getFieldValue(requestMappingHandlerAdapter, "sessionAttributesHandlerCache")).remove(bean.getClass());
            ((Map<?, ?>) FieldUtils.getFieldValue(requestMappingHandlerAdapter, "initBinderCache")).remove(bean.getClass());
            ((Map<?, ?>) FieldUtils.getFieldValue(requestMappingHandlerAdapter, "modelAttributeCache")).remove(bean.getClass());
            ((DefaultListableBeanFactory) ((AnnotationConfigApplicationContext) pluginContent).getBeanFactory()).destroySingleton(name);
        });
        requestMappings.remove(plugin.getPluginId());
    }

    private void printRegisterSuccessController(PluginWrapper plugin, Set<RequestMappingInfo> requestMappingInfos) {
        requestMappingInfos.forEach(requestMappingInfo -> log.info("plugin: {} register interface: {}", plugin.getPluginId(), requestMappingInfo));
    }

    private Set<RequestMappingInfo> registerController(Object bean) {
        Set<RequestMappingInfo> requestMappingInfos = new HashSet<>();
        Method[] methods = bean.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (!PluginUtils.isHaveRequestMapping(method)) {
                continue;
            }
            try {
                RequestMappingInfo requestMappingInfo = (RequestMappingInfo)
                        Objects.requireNonNull(getMappingForMethod()).invoke(getRequestMappingHandlerMapping(), method, bean.getClass());
                getRequestMappingHandlerMapping().registerMapping(requestMappingInfo, bean, method);
                requestMappingInfos.add(requestMappingInfo);
            } catch (Exception e) {
                log.error("interface register error: ", e);
            }
        }
        return requestMappingInfos;
    }

    private void unRegisterController(RequestMappingInfo requestMappingInfo) {
        getRequestMappingHandlerMapping().unregisterMapping(requestMappingInfo);
    }

    private Method getMappingForMethod() {
        try {
            Method method = ReflectUtils.findDeclaredMethod(getRequestMappingHandlerMapping().getClass(), "getMappingForMethod", new Class[]{Method.class, Class.class});
            method.setAccessible(true);
            return method;
        } catch (Exception ex) {
            log.error("reflect get mapping for method error:", ex);
        }
        return null;
    }

    private RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
        return (RequestMappingHandlerMapping) applicationContext.getBean("requestMappingHandlerMapping");
    }
}
