package org.openwes.mq;

import org.openwes.common.utils.user.UserContext;
import org.openwes.common.utils.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MqClient {

    @Value("${mq.type:redis}")
    private String mqType;

    private final RedisUtils redisUtils;

    public void sendMessage(String topic, Object message) {
        MqWrapper mqWrapper = new MqWrapper().setUsername(UserContext.getCurrentUser())
                .setTenantId("")
                .setMessage(message);

        if (mqType.equals("rabbitmq")) {
            log.info("rabbitmq");
        } else if (mqType.equals("kafka")) {
            log.info("kafka");
        } else if (mqType.equals("redis")) {
            redisUtils.publish(topic, mqWrapper);
        }
    }
}
