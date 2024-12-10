package org.openwes.common.utils.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.lang.reflect.Type;
import java.math.BigInteger;

@Configuration
public class PrecisionSolverConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MappingJackson2HttpMessageConverter getMappingJackson2HttpMessageConverter() {
        CustomMappingJackson2HttpMessageConverter jackson2HttpMessageConverter = new CustomMappingJackson2HttpMessageConverter();
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(BigInteger.class, ToStringSerializer.instance);
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
        simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        objectMapper.registerModule(simpleModule);
        objectMapper.registerModule(new JavaTimeModule());
        jackson2HttpMessageConverter.setObjectMapper(objectMapper);
        return jackson2HttpMessageConverter;
    }

    @Slf4j
    public static class CustomMappingJackson2HttpMessageConverter extends MappingJackson2HttpMessageConverter {

        /**
         * 判断该转换器是否能将请求内容转换成 Java 对象
         */
        @Override
        public boolean canRead(Class<?> clazz, MediaType mediaType) {
            // 不需要反序列化
            return false;
        }

        /**
         * 判断该转换器是否能将请求内容转换成 Java 对象
         */
        @Override
        public boolean canRead(Type type, Class<?> contextClass, MediaType mediaType) {
            // 不需要反序列化
            return false;
        }

        /**
         * 判断该转换器是否可以将 Java 对象转换成返回内容.
         * 匹配web api(形如/web/xxxx)中的接口方法的返回参数
         */
        @Override
        public boolean canWrite(Class<?> clazz, MediaType mediaType) {
            return true;
        }

    }
}
