package org.openwes.ai.core.config;

import org.openwes.ai.core.domain.LimitSizeInMemoryChatMemory;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public ChatMemory chatMemory() {
        return new LimitSizeInMemoryChatMemory(10);
    }
}
