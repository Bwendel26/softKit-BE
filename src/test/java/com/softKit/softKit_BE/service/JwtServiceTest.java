package com.softKit.softKit_BE.service;

import com.softKit.softKit_BE.config.JwtProperties;
import com.softKit.softKit_BE.model.User;
import com.softKit.softKit_BE.model.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;;

class JwtServiceTest {

	private JwtService jwtService;

	@BeforeEach
	void setUp() {
		JwtProperties props = new JwtProperties();
		props.setSecret("super-secret-jwt-key-for-tests-1234567890"); // >32 bytes, cai no branch UTF-8
		props.setExpirationMs(3600000L); // 1h

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