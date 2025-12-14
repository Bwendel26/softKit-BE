package com.softKit.softKit_BE.auth.application;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import static com.softKit.softKit_BE.testSupport.JwtTestSupport.*;
import static com.softKit.softKit_BE.testSupport.SecurityTestUsers.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

	// 1h para evitar expirar durante a suite
	private static final long EXP_MS = 3_600_000L;

	@Test
	void constructor_shouldAcceptBase64Secret_32BytesOrMore() {
		String base64Secret = base64SecretOfBytes(32);
		assertDoesNotThrow(() -> newService(base64Secret, EXP_MS));
	}

	@Test
	void constructor_shouldFallbackToUtf8Bytes_whenSecretIsNotBase64() {
		String invalidBase64ButLong = "this-is-not-base64!!-but-long-enough-to-be-32-bytes-minimum";
		assertDoesNotThrow(() -> newService(invalidBase64ButLong, EXP_MS));
	}

	@Test
	void constructor_shouldThrow_whenSecretIsNull() {
		IllegalArgumentException ex =
				assertThrows(IllegalArgumentException.class, () -> newService(null, EXP_MS));

		assertThat(ex.getMessage()).contains("JWT secret is too short");
	}

	@Test
	void constructor_shouldThrow_whenSecretIsTooShort_plainText() {
		String tooShort = "short-secret";

		IllegalArgumentException ex =
				assertThrows(IllegalArgumentException.class, () -> newService(tooShort, EXP_MS));

		assertThat(ex.getMessage()).contains("JWT secret is too short");
	}

	@Test
	void constructor_shouldThrow_whenSecretIsBase64ButDecodedTooShort() {
		String base64Secret16 = base64SecretOfBytes(16);

		IllegalArgumentException ex =
				assertThrows(IllegalArgumentException.class, () -> newService(base64Secret16, EXP_MS));

		assertThat(ex.getMessage()).contains("JWT secret is too short");
	}

	@Test
	void getExpirationMs_shouldReturnValueFromProperties() {
		JwtService service = newService(base64SecretOfBytes(32), 12345L);
		assertThat(service.getExpirationMs()).isEqualTo(12345L);
	}

	@Test
	void generateToken_shouldCreateSignedJwt_withSubjectRolesIatAndExp() {
		JwtService service = newService(base64SecretOfBytes(32), EXP_MS);

		UserDetails john = user("john@example.com", "ROLE_USER", "ROLE_ADMIN");

		String token = service.generateToken(john);
		assertThat(token).isNotBlank();

		Claims claims = parseClaims(service, token);

		assertThat(claims.getSubject()).isEqualTo("john@example.com");

		Object rolesObj = claims.get("roles");
		assertThat(rolesObj).isInstanceOf(List.class);

		@SuppressWarnings("unchecked")
		List<Object> roles = (List<Object>) rolesObj;
		assertThat(roles).contains("ROLE_USER", "ROLE_ADMIN");

		assertThat(claims.getIssuedAt()).isNotNull();
		assertThat(claims.getExpiration()).isNotNull();
		assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());

		long iat = claims.getIssuedAt().getTime();
		long exp = claims.getExpiration().getTime();
		assertThat(exp).isBetween(iat + EXP_MS - 2_000L, iat + EXP_MS + 2_000L);
	}

	@Test
	void extractUsername_shouldReturnSubject() {
		JwtService service = newService(base64SecretOfBytes(32), EXP_MS);

		UserDetails john = user("john@example.com", "ROLE_USER");
		String token = service.generateToken(john);

		assertThat(service.extractUsername(token)).isEqualTo("john@example.com");
	}

	@Test
	void extractUsername_shouldThrow_whenTokenIsInvalid() {
		JwtService service = newService(base64SecretOfBytes(32), EXP_MS);
		assertThrows(Exception.class, () -> service.extractUsername("not-a-jwt"));
	}

	@Test
	void isTokenValid_shouldReturnTrue_whenUsernameMatchesIgnoringCase_andNotExpired() {
		JwtService service = newService(base64SecretOfBytes(32), EXP_MS);

		String token = token(
				service,
				"john@example.com",
				Date.from(Instant.now()),
				Date.from(Instant.now().plusSeconds(3600))
		);

		UserDetails userUpper = user("JOHN@EXAMPLE.COM", "ROLE_USER");
		assertThat(service.isTokenValid(token, userUpper)).isTrue();
	}

	@Test
	void isTokenValid_shouldReturnFalse_whenUsernameIsNullInToken() {
		JwtService service = newService(base64SecretOfBytes(32), EXP_MS);

		String token = token(
				service,
				null,
				Date.from(Instant.now()),
				Date.from(Instant.now().plusSeconds(3600))
		);

		UserDetails john = user("john@example.com", "ROLE_USER");
		assertThat(service.isTokenValid(token, john)).isFalse();
	}

	@Test
	void isTokenValid_shouldReturnFalse_whenUsernameDoesNotMatch() {
		JwtService service = newService(base64SecretOfBytes(32), EXP_MS);

		String token = token(
				service,
				"john@example.com",
				Date.from(Instant.now()),
				Date.from(Instant.now().plusSeconds(3600))
		);

		UserDetails other = user("mary@example.com", "ROLE_USER");
		assertThat(service.isTokenValid(token, other)).isFalse();
	}

	@Test
	void isTokenValid_shouldThrowExpiredJwtException_whenTokenExpired() {
		JwtService service = newService(base64SecretOfBytes(32), EXP_MS);

		String expiredToken = token(
				service,
				"john@example.com",
				Date.from(Instant.now().minusSeconds(7200)),
				Date.from(Instant.now().minusSeconds(3600))
		);

		UserDetails john = user("john@example.com", "ROLE_USER");

		assertThrows(ExpiredJwtException.class,
				() -> service.isTokenValid(expiredToken, john));
	}

	@Test
	void isTokenValid_shouldReturnFalse_whenTokenHasNoExpiration() {
		JwtService service = newService(base64SecretOfBytes(32), EXP_MS);

		String tokenWithoutExp = tokenWithoutExpiration(
				service,
				"john@example.com",
				new Date()
		);

		UserDetails john = user("john@example.com", "ROLE_USER");
		assertThat(service.isTokenValid(tokenWithoutExp, john)).isFalse();
	}
}