package com.ridesharing.driverlocationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class DriverLocationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DriverLocationServiceApplication.class, args);
    }
}
