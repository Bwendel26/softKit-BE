package com.softKit.softKit_BE;

import com.softKit.softKit_BE.config.CorsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(CorsProperties.class)
public class SoftKitBeApplication {

	public static void main(String[] args) {
		SpringApplication.run(SoftKitBeApplication.class, args);
	}
}
