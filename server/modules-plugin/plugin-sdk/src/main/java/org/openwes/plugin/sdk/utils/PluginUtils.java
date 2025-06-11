package org.openwes.plugin.sdk.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 插件工具类
 */
@Slf4j
public class PluginUtils {

    public static final String CLASSES = "classes";
    public static final String LIB = "lib";
    public static final String CLASS_SUFFIX = ".class";
    public static final String PLUGIN_PROPERTIES = "plugin.properties";

    public static Set<String> readJarFile(String jarAddress) {
        Set<String> classNameSet = new HashSet<>();

        try (JarFile jarFile = new JarFile(jarAddress)) {
            Enumeration<JarEntry> entries = jarFile.entries();//遍历整个jar文件
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                String name = jarEntry.getName();
                //读取非依赖包的类文件
                if (!name.startsWith(LIB) && name.endsWith(CLASS_SUFFIX)) {
                    String className = name
                            .replaceFirst(CLASSES + "/", "")
                            .replace(CLASS_SUFFIX, "")
                            .replaceAll("/", ".");
                    classNameSet.add(className);
                }
            }
        } catch (Exception e) {
            log.warn("read jar file: {} error: ", jarAddress, e);
        }
        return classNameSet;
    }

    public static String getPluginScanPackages(File jarAddress, String fileName) {

        try (JarFile jarFile = new JarFile(jarAddress)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                String name = jarEntry.getName();
                if (name.contains(fileName)) {
                    Properties properties = new Properties();
                    try {
                        properties.load(jarFile.getInputStream(jarEntry));
                    } catch (IOException e) {
                        log.warn("get plugin scan-packages form file jar: {}, file name: {} error: ", jarAddress.getName(), fileName, e);
                    }

                    return properties.getProperty("plugin.scan-packages");

                }
            }
        } catch (Exception e) {
            log.warn("read file jar: {}, file name: {} specified file error: ", jarAddress.getName(), fileName, e);
        }
        return null;
    }

    public static boolean isController(Class<?> beanType) {
        return (AnnotatedElementUtils.hasAnnotation(beanType, Controller.class)
                || AnnotatedElementUtils.hasAnnotation(beanType, RestController.class)
                || AnnotatedElementUtils.hasAnnotation(beanType, RequestMapping.class));
    }

    public static boolean isHaveRequestMapping(Method method) {
        return AnnotatedElementUtils.hasAnnotation(method, RequestMapping.class);
    }

}

