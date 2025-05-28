package org.openwes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(scanBasePackages = {"org.openwes"})
public class PluginApplication {

    public static void main(String[] args) {
        SpringApplication.run(PluginApplication.class, args);
    }

}
