package com.example.demo;

import com.example.demo.config.DatabaseUrlConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan("com.example.demo.entity")
public class ToDoApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(ToDoApplication.class);
		app.addListeners(new DatabaseUrlConverter());
		app.run(args);
	}

}
