package com.softKit.softKit_BE.auth.api.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softKit.softKit_BE.testSupport.UserResponseArbitrariesSupport;
import com.softKit.softKit_BE.user.api.dto.UserResponse;
import net.jqwik.api.*;

import static org.assertj.core.api.Assertions.assertThat;

class LoginResponseTest extends UserResponseArbitrariesSupport {

	private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

	@Provide
	Arbitrary<String> tokenLikeNullable() {
		char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_.=".toCharArray();
		return Arbitraries.strings()
				.withChars(chars)
				.ofMinLength(0)
				.ofMaxLength(512)
				.injectNull(0.15);
	}

	@Provide
	Arbitrary<String> tokenTypeLikeNullable() {
		return Arbitraries.of("Bearer", "bearer", "JWT", "Token")
				.injectNull(0.10);
	}

	@Provide
	Arbitrary<Long> expiresInSeconds() {
		return Arbitraries.longs().between(0L, 31_536_000L);
	}

	@Property(tries = 150)
	void recordStoresValuesCorrectly(
			@ForAll("tokenLikeNullable") String token,
			@ForAll("tokenTypeLikeNullable") String tokenType,
			@ForAll("expiresInSeconds") long expiresIn,
			@ForAll("userResponsesNullable") UserResponse user
	) {
		LoginResponse res = new LoginResponse(token, tokenType, expiresIn, user);

		assertThat(res.token()).isEqualTo(token);
		assertThat(res.tokenType()).isEqualTo(tokenType);
		assertThat(res.expiresIn()).isEqualTo(expiresIn);
		assertThat(res.user()).isEqualTo(user);
	}

	@Property(tries = 120)
	void equalsAndHashCode_sameValues_areEqual(
			@ForAll("tokenLikeNullable") String token,
			@ForAll("tokenTypeLikeNullable") String tokenType,
			@ForAll("expiresInSeconds") long expiresIn,
			@ForAll("userResponsesNullable") UserResponse user
	) {
		LoginResponse a = new LoginResponse(token, tokenType, expiresIn, user);
		LoginResponse b = new LoginResponse(token, tokenType, expiresIn, user);

		assertThat(a).isEqualTo(b);
		assertThat(a.hashCode()).isEqualTo(b.hashCode());
	}

	@Property(tries = 120)
	void jacksonRoundTrip_preservesValues(
			@ForAll("tokenLikeNullable") String token,
			@ForAll("tokenTypeLikeNullable") String tokenType,
			@ForAll("expiresInSeconds") long expiresIn,
			@ForAll("userResponsesNullable") UserResponse user
	) throws Exception {
		LoginResponse original = new LoginResponse(token, tokenType, expiresIn, user);

		String json = objectMapper.writeValueAsString(original);
		LoginResponse back = objectMapper.readValue(json, LoginResponse.class);

		assertThat(back).isEqualTo(original);
	}
}