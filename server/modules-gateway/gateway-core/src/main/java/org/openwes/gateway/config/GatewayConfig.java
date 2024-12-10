package org.openwes.gateway.config;

import org.openwes.common.utils.utils.RedisUtils;
import org.openwes.gateway.filter.GlobalGatewayFilter;
import org.openwes.user.api.utils.JwtUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public GlobalGatewayFilter authGatewayFilter(AuthProperties authProperties, JwtUtils jwtUtils, RedisUtils redisUtils) {
        return new GlobalGatewayFilter(authProperties, jwtUtils, redisUtils);
    }
}
