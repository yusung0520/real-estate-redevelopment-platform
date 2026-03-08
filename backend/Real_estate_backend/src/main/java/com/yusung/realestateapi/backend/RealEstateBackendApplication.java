package com.yusung.realestateapi.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class RealEstateBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(RealEstateBackendApplication.class, args);
    }

}
