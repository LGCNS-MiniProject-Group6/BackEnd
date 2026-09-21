package com.miniproject1.miniproject1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class Miniproject1Application {

	public static void main(String[] args) {
		SpringApplication.run(Miniproject1Application.class, args);
	}
}