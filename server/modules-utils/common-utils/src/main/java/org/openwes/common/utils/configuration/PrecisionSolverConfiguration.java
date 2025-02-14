package org.openwes.common.utils.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.math.BigInteger;

@Configuration
public class PrecisionSolverConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MappingJackson2HttpMessageConverter getMappingJackson2HttpMessageConverter() {
        ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json()
                .modules(new JavaTimeModule(), new SimpleModule()
                        .addSerializer(BigInteger.class, ToStringSerializer.instance)
                        .addSerializer(Long.class, ToStringSerializer.instance)
                        .addSerializer(Long.TYPE, ToStringSerializer.instance))
                .build();
        return new MappingJackson2HttpMessageConverter(objectMapper);
    }

}
