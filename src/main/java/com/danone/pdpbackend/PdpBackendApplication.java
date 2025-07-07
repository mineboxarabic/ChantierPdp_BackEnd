package com.danone.pdpbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.danone.pdpbackend.Repo") // Ensure only `Repo` package is scanned
public class PdpBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(PdpBackendApplication.class, args);
    }

 }
