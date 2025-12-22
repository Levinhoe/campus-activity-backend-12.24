package com.campus.activity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class CampusActivityApplication {
    public static void main(String[] args) {
        SpringApplication.run(CampusActivityApplication.class, args);
    }
}
