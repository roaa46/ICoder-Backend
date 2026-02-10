package com.icoder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication
@EnableAsync
public class ICoderApplication {
    public static void main(String[] args) {
        SpringApplication.run(ICoderApplication.class, args);
    }
}
