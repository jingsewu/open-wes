package org.openwes.simulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class RcsSimulatorApplication {
    public static void main(String[] args) {
        SpringApplication.run(RcsSimulatorApplication.class, args);
    }
}
