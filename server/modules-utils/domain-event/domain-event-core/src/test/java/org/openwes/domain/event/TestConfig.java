package org.openwes.domain.event;

import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableAutoConfiguration(exclude = {RedissonAutoConfiguration.class})
@EnableAspectJAutoProxy
@ComponentScan("org.openwes.domain.event")
@EnableJpaAuditing
public class TestConfig {

}
