package com.newssum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.newssum.security.JwtProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class NewsSumApplication {

    public static void main(String[] args) {
        SpringApplication.run(NewsSumApplication.class, args);
    }
}