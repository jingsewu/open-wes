package org.openwes.plugin.sdk.utils;

import org.openwes.plugin.extension.config.ConfigParam;
import org.openwes.plugin.extension.config.IExtensionConfig;
import org.openwes.plugin.extension.config.InputType;

import java.util.Arrays;
import java.util.List;

public class ConfigMetadataExtractor {
    public static List<ConfigFieldMetadata> extractMetadata(Class<? extends IExtensionConfig> configClass) {
        return Arrays.stream(configClass.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(ConfigParam.class))
                .map(f -> {
                    ConfigParam annotation = f.getAnnotation(ConfigParam.class);
                    return new ConfigFieldMetadata(
                            f.getName(),
                            annotation.label(),
                            annotation.description(),
                            annotation.type(),
                            annotation.required(),
                            annotation.options(),
                            annotation.validationRegex(),
                            annotation.min(),
                            annotation.max()
                    );
                }).toList();
    }

    public record ConfigFieldMetadata(
            String fieldName,
            String label,
            String description,
            InputType type,
            boolean required,
            String[] options,
            String validationRegex,
            double min,
            double max
    ) {
    }
}

