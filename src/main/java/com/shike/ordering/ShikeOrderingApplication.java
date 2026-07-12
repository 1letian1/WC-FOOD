package com.shike.ordering;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ShikeOrderingApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShikeOrderingApplication.class, args);
    }
}
