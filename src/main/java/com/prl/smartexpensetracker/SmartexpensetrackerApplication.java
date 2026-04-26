package com.prl.smartexpensetracker;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(
    info = @Info(
        title = "SmartexpensetrackerApplication API",
        version = "1.0",
        description = "API documentation for SmartexpensetrackerApplication"
    )
)
@SpringBootApplication
public class SmartexpensetrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartexpensetrackerApplication.class, args);
    }
}