package com.creditsimulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class CreditSimulatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(CreditSimulatorApplication.class, args);
    }

}
