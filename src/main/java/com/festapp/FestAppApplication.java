package com.festapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FestAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(FestAppApplication.class, args);
    }
}