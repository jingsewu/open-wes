package org.openwes;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(scanBasePackages = {"org.openwes"})
@Slf4j
public class WesApplication {

    public static void main(String[] args) {
        SpringApplication.run(WesApplication.class, args);
    }

}
