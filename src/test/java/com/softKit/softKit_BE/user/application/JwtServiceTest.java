package com.softKit.softKit_BE.user.application;

import com.softKit.softKit_BE.auth.application.JwtService;
import com.softKit.softKit_BE.shared.config.jwt.JwtProperties;
import com.softKit.softKit_BE.user.domain.User;
import com.softKit.softKit_BE.user.domain.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;;

class JwtServiceTest {

	private JwtService jwtService;

	@BeforeEach
	void setUp() {
		JwtProperties props = new JwtProperties();
		String secret = "this-is-a-super-secret-key-at-least-32-bytes";
		String base64Secret = Base64.getEncoder().encodeToString(secret.getBytes(StandardCharsets.UTF_8));
		props.setSecret(base64Secret);
		props.setExpirationMs(3600000L);

		jwtService = new JwtService(props);
	}

	private User buildUser() {
		return new User("John Doe", "john@example.com", "hashed", Role.CUSTOMER);
	}

	@Test
	void generateToken_and_extractUsername_shouldWork() {
		UserDetails user = buildUser();

		String token = jwtService.generateToken(user);

		assertNotNull(token);

		String username = jwtService.extractUsername(token);
		assertEquals("john@example.com", username);
	}

	@Test
	void isTokenValid_shouldReturnTrue_forValidTokenAndUser() {
		UserDetails user = buildUser();

		String token = jwtService.generateToken(user);

		assertTrue(jwtService.isTokenValid(token, user));
	}

	@Test
	void isTokenValid_shouldReturnFalse_forDifferentUser() {
		UserDetails user = buildUser();
		String token = jwtService.generateToken(user);

		User another = new User("Jane", "jane@example.com", "hashed2", Role.CUSTOMER);

		assertFalse(jwtService.isTokenValid(token, another));
	}

}