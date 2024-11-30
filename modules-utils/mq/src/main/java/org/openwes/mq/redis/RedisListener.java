package org.openwes.mq.redis;

import org.redisson.codec.JsonJacksonCodec;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisListener {

    String topic() default "";

    /**
     * type of message
     *
     * @return
     */
    Class<?> type() default Object.class;

    Class<JsonJacksonCodec> serializer() default JsonJacksonCodec.class;
}
