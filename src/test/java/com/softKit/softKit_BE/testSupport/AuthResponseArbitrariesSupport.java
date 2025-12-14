package com.softKit.softKit_BE.testSupport;

import net.jqwik.api.*;

public abstract class AuthResponseArbitrariesSupport extends UserResponseArbitrariesSupport {

	@Provide
	public Arbitrary<String> tokenTypeLike() {
		// padrões comuns
		return Arbitraries.of("Bearer", "bearer", "JWT", "Token")
				.injectNull(0.10);
	}

	@Provide
	public Arbitrary<Long> expiresInSeconds() {
		// 0 .. 365 dias (em segundos) - realista e sem negativos
		return Arbitraries.longs().between(0L, 31_536_000L);
	}

	@Provide
	public Arbitrary<String> jwtLike() {
		// Base64URL-ish (sem '=') com 3 segmentos separados por "."
		char[] b64url = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_".toCharArray();

		Arbitrary<String> seg = Arbitraries.strings()
				.withChars(b64url)
				.ofMinLength(10)
				.ofMaxLength(120);

		return Combinators.combine(seg, seg, seg)
				.as((a, b, c) -> a + "." + b + "." + c);
	}

	@Provide
	public Arbitrary<String> tokenLikeNullable() {
		// Mistura token estilo JWT + tokens aleatórios + null
		char[] tokenChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_.=:/+~".toCharArray();

		Arbitrary<String> randomToken = Arbitraries.strings()
				.withChars(tokenChars)
				.ofMinLength(1)
				.ofMaxLength(512);

		return Arbitraries.oneOf(jwtLike(), randomToken)
				.injectNull(0.15);
	}
}
