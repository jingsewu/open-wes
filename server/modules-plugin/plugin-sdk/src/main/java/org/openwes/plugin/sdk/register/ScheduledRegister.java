package org.openwes.plugin.sdk.register;

import lombok.RequiredArgsConstructor;
import org.pf4j.PluginWrapper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.scheduling.config.TaskManagementConfigUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScheduledRegister extends AbstractRegister {

    @Override
    public void beforeRegister(ApplicationContext pluginContext, PluginWrapper Plugin) {
        if (pluginContext instanceof AnnotationConfigApplicationContext annotationConfigApplicationContext) {
            annotationConfigApplicationContext
                    .registerBean(TaskManagementConfigUtils.SCHEDULED_ANNOTATION_PROCESSOR_BEAN_NAME, ScheduledAnnotationBeanPostProcessor.class);
        }
    }

    @Override
    public void unRegister(ApplicationContext pluginContext, PluginWrapper Plugin) {
        Object bean = pluginContext.getBean(TaskManagementConfigUtils.SCHEDULED_ANNOTATION_PROCESSOR_BEAN_NAME);
        ((ScheduledAnnotationBeanPostProcessor) bean).destroy();
    }
}
