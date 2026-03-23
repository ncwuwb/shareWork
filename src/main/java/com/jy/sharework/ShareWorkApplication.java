package com.jy.sharework;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ShareWorkApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShareWorkApplication.class, args);
    }

}
