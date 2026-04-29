package com.multivendor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MultivendorApplication {
    public static void main(String[] args) {
        SpringApplication.run(MultivendorApplication.class, args);
    }
}
