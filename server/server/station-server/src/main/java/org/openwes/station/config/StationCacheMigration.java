package org.openwes.station.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class StationCacheMigration implements ApplicationRunner {
    private final StringRedisTemplate redisTemplate;

    @Override
    public void run(ApplicationArguments args) {
        Set<String> keys = redisTemplate.keys("WorkStation:*");
        if (keys != null && !keys.isEmpty()) {
            log.info("Clearing {} stale WorkStation cache entries for schema migration", keys.size());
            redisTemplate.delete(keys);
        }
    }
}
