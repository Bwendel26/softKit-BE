package com.softKit.softKit_BE.auth.application;

import com.softKit.softKit_BE.auth.application.mapper.AuthMapper;
import com.softKit.softKit_BE.shared.exception.EmailAlreadyInUseException;
import com.softKit.softKit_BE.user.domain.User;
import com.softKit.softKit_BE.auth.api.dto.LoginRequest;
import com.softKit.softKit_BE.auth.api.dto.RegisterRequest;
import com.softKit.softKit_BE.auth.api.dto.LoginResponse;
import com.softKit.softKit_BE.auth.api.dto.RegisterResponse;
import com.softKit.softKit_BE.user.api.dto.UserResponse;
import com.softKit.softKit_BE.user.domain.enums.Role;
import com.softKit.softKit_BE.user.domain.enums.Status;
import com.softKit.softKit_BE.user.application.mapper.UserMapper;
import com.softKit.softKit_BE.user.infrastructure.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private AuthMapper authMapper;

	@Mock
	private UserMapper userMapper;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtService jwtService;

	@Mock
	private AuthenticationManager authenticationManager;

	@InjectMocks
	private AuthService authService;

	@Test
	void login_shouldReturnTokenAndUser_whenCredentialsAreValid() {
		LoginRequest request = new LoginRequest("john@example.com", "Password123");

		User user = new User("John Doe", "john@example.com", "hashed", Role.CUSTOMER);
		user.setStatus(Status.ACTIVE);

		Authentication authentication =
				new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

		when(authenticationManager.authenticate(any(Authentication.class)))
				.thenReturn(authentication);

		when(jwtService.generateToken(user)).thenReturn("jwt-token");
		when(jwtService.getExpirationMs()).thenReturn(3600000L);
		when(userMapper.toUserResponse(user))
				.thenReturn(new UserResponse(
						UUID.randomUUID(),
						"John Doe",
						"john@example.com",
						"+5511999999999",
						Role.CUSTOMER,
						Status.ACTIVE,
						null,
						null,
						OffsetDateTime.now(),
						OffsetDateTime.now()
				));

		LoginResponse response = authService.login(request);

		assertEquals("jwt-token", response.token());
		assertEquals("Bearer", response.tokenType());
		assertEquals(3600000L, response.expiresIn());
		assertNotNull(response.user());
		assertEquals("john@example.com", response.user().email());
	}

	@Test
	void login_shouldThrowBadCredentials_whenAuthenticationFails() {
		LoginRequest request = new LoginRequest("john@example.com", "wrong");

		when(authenticationManager.authenticate(any(Authentication.class)))
				.thenThrow(new BadCredentialsException("Invalid username or password"));

		assertThrows(BadCredentialsException.class,
				() -> authService.login(request));
	}

	@Test
	void register_shouldCreateUser_whenEmailNotInUse() {
		RegisterRequest request = new RegisterRequest(
				"John Doe",
				"john@example.com",
				"+5511999999999",
				"Password123"
		);

		when(userRepository.existsByEmail("john@example.com")).thenReturn(false);

		User user = new User("John Doe", "john@example.com", null, Role.CUSTOMER);
		when(authMapper.fromRegisterRequestToEntity(request)).thenReturn(user);
		when(passwordEncoder.encode("Password123")).thenReturn("hashed");
		user.setPasswordHash("hashed");
		user.setStatus(Status.ACTIVE);

		User savedUser = user;
		savedUser.setEmailVerifiedAt(OffsetDateTime.now());
		when(userRepository.save(user)).thenReturn(savedUser);

		RegisterResponse registerResponse = new RegisterResponse(
				UUID.randomUUID(),
				"John Doe",
				"john@example.com",
				"+5511999999999",
				OffsetDateTime.now(),
				OffsetDateTime.now()
		);
		when(authMapper.toRegisterResponse(savedUser)).thenReturn(registerResponse);

		RegisterResponse response = authService.register(request);

		assertEquals("john@example.com", response.email());
		verify(userRepository).existsByEmail("john@example.com");
		verify(userRepository).save(user);
	}

	@Test
	void register_shouldThrowEmailAlreadyInUse_whenExists() {
		RegisterRequest request = new RegisterRequest(
				"John Doe",
				"john@example.com",
				"+5511999999999",
				"Password123"
		);

		when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

		assertThrows(EmailAlreadyInUseException.class,
				() -> authService.register(request));

		verify(userRepository, never()).save(any());
	}

	@Test
	void validateToken_shouldReturnTrue_whenTokenValidAndUserExists() {
		String token = "jwt-token";
		User user = new User();
		user.setUsername("john@example.com");

		when(jwtService.extractUsername(token)).thenReturn("john@example.com");
		when(userRepository.findByEmail("john@example.com")).thenReturn(user);
		when(jwtService.isTokenValid(token, user)).thenReturn(true);

		boolean result = authService.validateToken(token);

		assertThat(result).isTrue();
	}

	@Test
	void validateToken_shouldReturnFalse_whenUserDoesNotExist() {
		String token = "jwt-token";

		when(jwtService.extractUsername(token)).thenReturn("john@example.com");
		when(userRepository.findByEmail("john@example.com")).thenReturn(null);

		boolean result = authService.validateToken(token);

		assertThat(result).isFalse();
	}

	@Test
	void validateToken_shouldReturnFalse_whenJwtServiceThrows() {
		String token = "invalid-token";
		when(jwtService.extractUsername(token)).thenThrow(new RuntimeException("Invalid"));

		boolean result = authService.validateToken(token);

		assertThat(result).isFalse();
	}

	@Test
	void extractEmailFromToken_shouldDelegateToJwtService() {
		String token = "jwt-token";
		when(jwtService.extractUsername(token)).thenReturn("john@example.com");

		String email = authService.extractEmailFromToken(token);

		assertThat(email).isEqualTo("john@example.com");
		verify(jwtService).extractUsername(token);
	}
}