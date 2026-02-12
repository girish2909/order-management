package com.example.ordermanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class OrderManagementBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderManagementBackendApplication.class, args);
    }

}
