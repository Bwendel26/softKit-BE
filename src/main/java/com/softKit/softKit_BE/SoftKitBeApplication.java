package com.softKit.softKit_BE;

import com.softKit.softKit_BE.shared.config.security.CorsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(CorsProperties.class)
public class SoftKitBeApplication {

	public static void main(String[] args) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(SoftKitBeApplication.class);
        builder.lazyInitialization(true);
		SpringApplication.run(SoftKitBeApplication.class, args);
	}
}
