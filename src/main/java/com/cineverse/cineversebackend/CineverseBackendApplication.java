package com.cineverse.cineversebackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CineverseBackendApplication {

    public static void main(String[] args) {

        System.out.println("=================================");
        System.out.println("MONGO URI = " + System.getenv("SPRING_DATA_MONGODB_URI"));
        System.out.println("PORT = " + System.getenv("PORT"));
        System.out.println("=================================");

        SpringApplication.run(CineverseBackendApplication.class, args);
    }
}