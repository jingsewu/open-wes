package org.openwes.common.utils.configuration;

import jakarta.validation.constraints.NotNull;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
@ConditionalOnClass(RedissonClient.class)
public class CacheConfig {

    @Bean
    public RedisCacheConfiguration redisCacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.json()));
    }

    @Bean
    public RedisCacheWriter redisCacheWriter(RedisConnectionFactory redisConnectionFactory) {
        return RedisCacheWriter.lockingRedisCacheWriter(redisConnectionFactory);
    }

    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {
        return new TenantCacheManager(redisCacheWriter(redisConnectionFactory), redisCacheConfiguration());
    }

    public static class TenantCacheManager extends RedisCacheManager {

        public TenantCacheManager(RedisCacheWriter cacheWriter, RedisCacheConfiguration defaultCacheConfiguration) {
            super(cacheWriter, defaultCacheConfiguration);
            RedisCacheManager.builder()
                    .cacheWriter(cacheWriter)
                    .cacheDefaults(defaultCacheConfiguration)
                    .build();
        }

        /**
         * @param name
         * @return Prefix the cache store name with the TENANT KEY
         * For SUPER ADMIN no prefix applied
         */
        @Override
        public Cache getCache(@NotNull String name) {
            String tenantId = "";
            return super.getCache(tenantId + "_" + name);
        }
    }
}
