package org.openwes.station;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(scanBasePackages = {"org.openwes"}, exclude = {DataSourceAutoConfiguration.class})
public class StationApplication {

    public static void main(String[] args) {
        SpringApplication.run(StationApplication.class, args);
    }
}
