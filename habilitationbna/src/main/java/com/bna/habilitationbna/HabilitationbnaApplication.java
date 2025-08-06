package com.bna.habilitationbna;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.bna.habilitationbna")
@EnableScheduling
public class HabilitationbnaApplication {

    public static void main(String[] args) {
        SpringApplication.run(HabilitationbnaApplication.class, args);
    }

}
