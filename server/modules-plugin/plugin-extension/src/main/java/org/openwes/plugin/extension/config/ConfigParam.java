package org.openwes.plugin.extension.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Config Param annotation that on the field level.
 * for example:
 * public class MyPluginConfig extends IExtensionConfig {
 *
 * @ConfigParam( label = "API Key",
 * description = "Your service API key",
 * type = InputType.PASSWORD,
 * required = true
 * )
 * private String apiKey;
 * @ConfigParam( label = "Refresh Interval",
 * description = "Data refresh interval in minutes",
 * type = InputType.NUMBER,
 * min = 1,
 * max = 60
 * )
 * private int refreshInterval;
 * <p>
 * }
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigParam {

    String label() default "";

    String description() default "";

    InputType type() default InputType.TEXT;

    boolean required() default false;

    String[] options() default {};

    String validationRegex() default "";

    int min() default Integer.MIN_VALUE;

    int max() default Integer.MAX_VALUE;
}

