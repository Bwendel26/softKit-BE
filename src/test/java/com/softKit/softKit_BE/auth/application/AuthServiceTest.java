package com.softKit.softKit_BE.auth.application;

import com.softKit.softKit_BE.auth.api.dto.*;
import com.softKit.softKit_BE.auth.application.mapper.AuthMapper;
import com.softKit.softKit_BE.auth.domain.PasswordResetToken;
import com.softKit.softKit_BE.auth.infrastructure.PasswordResetTokenRepository;
import com.softKit.softKit_BE.shared.exception.EmailAlreadyInUseException;
import com.softKit.softKit_BE.shared.exception.InvalidTokenException;
import com.softKit.softKit_BE.user.domain.User;
import com.softKit.softKit_BE.user.api.dto.UserResponse;
import com.softKit.softKit_BE.user.domain.enums.Role;
import com.softKit.softKit_BE.user.domain.enums.Status;
import com.softKit.softKit_BE.user.application.mapper.UserMapper;
import com.softKit.softKit_BE.user.infrastructure.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.Optional;
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
	private PasswordResetTokenRepository passwordResetTokenRepository;

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

	@Test
	void refreshToken_shouldThrowBadCredentials_whenHeaderIsNull() {
		assertThrows(BadCredentialsException.class, () -> authService.refreshToken(null));
	}

	@Test
	void refreshToken_shouldThrowBadCredentials_whenHeaderDoesNotStartWithBearer() {
		assertThrows(BadCredentialsException.class, () -> authService.refreshToken("Basic abc"));
		assertThrows(BadCredentialsException.class, () -> authService.refreshToken("Bearer")); // sem espaço
		assertThrows(BadCredentialsException.class, () -> authService.refreshToken("BearerX abc"));
	}

	@Test
	void refreshToken_shouldReturnNewToken_whenTokenIsValidAndUserExists() {
		String authHeader = "Bearer old-token";
		String oldToken = "old-token";

		User user = new User("John Doe", "john@example.com", "hashed", Role.CUSTOMER);
		user.setStatus(Status.ACTIVE);

		when(jwtService.extractUsername(oldToken)).thenReturn("john@example.com");
		when(userRepository.findByEmail("john@example.com")).thenReturn(user);
		when(jwtService.isTokenValid(oldToken, user)).thenReturn(true);

		when(jwtService.generateToken(user)).thenReturn("new-token");
		when(jwtService.getExpirationMs()).thenReturn(3600000L);
		when(userMapper.toUserResponse(user)).thenReturn(minimalUserResponse("john@example.com"));

		LoginResponse response = authService.refreshToken(authHeader);

		assertThat(response.token()).isEqualTo("new-token");
		assertThat(response.tokenType()).isEqualTo("Bearer");
		assertThat(response.expiresIn()).isEqualTo(3600000L);
		assertThat(response.user()).isNotNull();
		assertThat(response.user().email()).isEqualTo("john@example.com");

		verify(jwtService).extractUsername(oldToken);
		verify(jwtService).isTokenValid(oldToken, user);
		verify(jwtService).generateToken(user);
	}

	@Test
	void refreshToken_shouldThrowBadCredentials_whenUserNotFound() {
		String authHeader = "Bearer any-token";
		String token = "any-token";

		when(jwtService.extractUsername(token)).thenReturn("john@example.com");
		when(userRepository.findByEmail("john@example.com")).thenReturn(null);

		BadCredentialsException ex =
				assertThrows(BadCredentialsException.class, () -> authService.refreshToken(authHeader));

		assertThat(ex.getMessage()).contains("User not found");
	}

	@Test
	void refreshToken_shouldThrowBadCredentials_whenTokenIsInvalid() {
		String authHeader = "Bearer bad-token";
		String token = "bad-token";

		User user = new User("John Doe", "john@example.com", "hashed", Role.CUSTOMER);

		when(jwtService.extractUsername(token)).thenReturn("john@example.com");
		when(userRepository.findByEmail("john@example.com")).thenReturn(user);
		when(jwtService.isTokenValid(token, user)).thenReturn(false);

		BadCredentialsException ex =
				assertThrows(BadCredentialsException.class, () -> authService.refreshToken(authHeader));

		assertThat(ex.getMessage()).contains("Invalid or expired token");
		verify(jwtService, never()).generateToken(any());
	}

	@Test
	void refreshToken_shouldRethrowAuthenticationException_withoutWrapping() {
		String authHeader = "Bearer token";
		BadCredentialsException original = new BadCredentialsException("boom");

		when(jwtService.extractUsername("token")).thenThrow(original);

		BadCredentialsException thrown =
				assertThrows(BadCredentialsException.class, () -> authService.refreshToken(authHeader));

		assertThat(thrown).isSameAs(original);
	}

	@Test
	void refreshToken_shouldWrapUnexpectedExceptions() {
		String authHeader = "Bearer token";

		when(jwtService.extractUsername("token")).thenThrow(new RuntimeException("kaboom"));

		BadCredentialsException ex =
				assertThrows(BadCredentialsException.class, () -> authService.refreshToken(authHeader));

		assertThat(ex.getMessage()).isEqualTo("Invalid or expired token");
		assertThat(ex.getCause()).isInstanceOf(RuntimeException.class);
	}

	@Test
	void forgotPassword_shouldDoNothing_whenUserDoesNotExist() {
		ForgotPasswordRequest request = new ForgotPasswordRequest("nobody@example.com");

		when(userRepository.findByEmail("nobody@example.com")).thenReturn(null);

		authService.forgotPassword(request);

		verify(passwordResetTokenRepository, never()).save(any());
	}

	@Test
	void forgotPassword_shouldCreateAndSaveResetToken_whenUserExists() {
		ForgotPasswordRequest request = new ForgotPasswordRequest("john@example.com");
		User user = new User("John Doe", "john@example.com", "hashed", Role.CUSTOMER);

		when(userRepository.findByEmail("john@example.com")).thenReturn(user);

		authService.forgotPassword(request);

		ArgumentCaptor<PasswordResetToken> captor = ArgumentCaptor.forClass(PasswordResetToken.class);
		verify(passwordResetTokenRepository).save(captor.capture());

		PasswordResetToken saved = captor.getValue();
		assertThat(saved.getUser()).isSameAs(user);
		assertThat(saved.getToken()).isNotBlank();
		assertThat(saved.isExpired()).isFalse(); // expiresAt deve estar no futuro
	}

	@Test
	void resetPassword_shouldThrowInvalidToken_whenTokenNotFound() {
		ResetPasswordRequest request = new ResetPasswordRequest(
				"missing-token",
				"NewPass123",
				"NewPass123"
		);

		when(passwordResetTokenRepository.findByToken("missing-token"))
				.thenReturn(Optional.empty());

		assertThrows(InvalidTokenException.class, () -> authService.resetPassword(request));
		verify(userRepository, never()).save(any());
	}

	@Test
	void resetPassword_shouldThrowInvalidToken_whenTokenIsExpired() {
		User user = new User("John Doe", "john@example.com", "old-hash", Role.CUSTOMER);

		PasswordResetToken token = new PasswordResetToken();
		token.setToken("t1");
		token.setUser(user);
		token.setExpiresAt(OffsetDateTime.now().minusMinutes(1)); // expirado

		when(passwordResetTokenRepository.findByToken("t1"))
				.thenReturn(Optional.of(token));

		ResetPasswordRequest request = new ResetPasswordRequest("t1", "NewPass123", "NewPass123");

		assertThrows(InvalidTokenException.class, () -> authService.resetPassword(request));
		verify(userRepository, never()).save(any());
		verify(passwordResetTokenRepository, never()).save(any());
	}

	@Test
	void resetPassword_shouldThrowInvalidToken_whenTokenIsUsed() {
		User user = new User("John Doe", "john@example.com", "old-hash", Role.CUSTOMER);

		PasswordResetToken token = new PasswordResetToken();
		token.setToken("t2");
		token.setUser(user);
		token.setExpiresAt(OffsetDateTime.now().plusMinutes(30));
		token.setUsedAt(OffsetDateTime.now().minusMinutes(5)); // já usado

		when(passwordResetTokenRepository.findByToken("t2"))
				.thenReturn(Optional.of(token));

		ResetPasswordRequest request = new ResetPasswordRequest("t2", "NewPass123", "NewPass123");

		assertThrows(InvalidTokenException.class, () -> authService.resetPassword(request));
		verify(userRepository, never()).save(any());
		verify(passwordResetTokenRepository, never()).save(any());
	}

	@Test
	void resetPassword_shouldThrowIllegalArgument_whenPasswordsDoNotMatch() {
		User user = new User("John Doe", "john@example.com", "old-hash", Role.CUSTOMER);

		PasswordResetToken token = new PasswordResetToken();
		token.setToken("t3");
		token.setUser(user);
		token.setExpiresAt(OffsetDateTime.now().plusMinutes(30));

		when(passwordResetTokenRepository.findByToken("t3"))
				.thenReturn(Optional.of(token));

		ResetPasswordRequest request = new ResetPasswordRequest("t3", "NewPass123", "Different123");

		assertThrows(IllegalArgumentException.class, () -> authService.resetPassword(request));
		verify(userRepository, never()).save(any());
		verify(passwordResetTokenRepository, never()).save(any());
	}

	@Test
	void resetPassword_shouldUpdatePasswordAndMarkTokenUsed_whenValid() {
		User user = new User("John Doe", "john@example.com", "old-hash", Role.CUSTOMER);

		PasswordResetToken token = new PasswordResetToken();
		token.setToken("t4");
		token.setUser(user);
		token.setExpiresAt(OffsetDateTime.now().plusMinutes(30));

		when(passwordResetTokenRepository.findByToken("t4"))
				.thenReturn(Optional.of(token));

		when(passwordEncoder.encode("NewPass123")).thenReturn("new-hash");
		when(userRepository.save(user)).thenReturn(user);
		when(passwordResetTokenRepository.save(token)).thenReturn(token);

		ResetPasswordRequest request = new ResetPasswordRequest("t4", "NewPass123", "NewPass123");

		authService.resetPassword(request);

		verify(passwordEncoder).encode("NewPass123");
		verify(userRepository).save(user);
		verify(passwordResetTokenRepository).save(token);

		// garante que marcou como usado
		assertThat(token.isUsed()).isTrue();
	}

	private UserResponse minimalUserResponse(String email) {
		return new UserResponse(
				UUID.randomUUID(),
				"John Doe",
				email,
				"+5511999999999",
				Role.CUSTOMER,
				Status.ACTIVE,
				null,
				null,
				OffsetDateTime.now(),
				OffsetDateTime.now()
		);
	}
}