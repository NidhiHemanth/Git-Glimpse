package org.bootcamp.com;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
 @SpringBootApplication - it a combination of the three annotations with their default attributes
                                @Configuration
                                @EnableAutoConfiguration
                                @ComponentScan
 */

@SpringBootApplication
public class BatchJobApplication {
    public static void main(String[] args) {
        SpringApplication.run(BatchJobApplication.class);
    }
}