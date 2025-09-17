package org.openwes;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableRetry
@SpringBootApplication(scanBasePackages = {"org.openwes"})
@Slf4j
public class WesApplication {

    public static void main(String[] args) {
        SpringApplication.run(WesApplication.class, args);
    }

}
