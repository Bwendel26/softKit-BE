package com.softKit.softKit_BE.testSupport;

import net.jqwik.api.*;

public abstract class PasswordArbitrariesSupport extends UserFieldArbitrariesSupport {

	@Provide
	public Arbitrary<String> validPasswords() {
		// Gera senha que sempre passa no regex:
		// - >= 8
		// - contém 1 dígito, 1 maiúscula, 1 minúscula
		// - total <= 255
		Arbitrary<String> lower = Arbitraries.strings()
				.withChars("abcdefghijklmnopqrstuvwxyz".toCharArray())
				.ofLength(1);

		Arbitrary<String> upper = Arbitraries.strings()
				.withChars("ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray())
				.ofLength(1);

		Arbitrary<String> digit = Arbitraries.strings()
				.withChars("0123456789".toCharArray())
				.ofLength(1);

		char[] bodyChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%*_-.".toCharArray();
		Arbitrary<String> body = Arbitraries.strings()
				.withChars(bodyChars)
				.ofMinLength(5)   // 1+1+1 + 5 = 8
				.ofMaxLength(252) // 3 fixos + 252 = 255
				.filter(s -> !s.isBlank());

		return Combinators.combine(lower, upper, digit, body)
				.as((l, u, d, b) -> l + u + d + b);
	}

	@Provide
	public Arbitrary<String> invalidPasswords() {
		// Cobrir falhas típicas:
		// - < 8
		// - sem dígito
		// - sem maiúscula
		// - sem minúscula
		return Arbitraries.oneOf(
				// curto demais
				Arbitraries.strings()
						.withChars("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray())
						.ofMinLength(0)
						.ofMaxLength(7),

				// sem dígito (só letras e símbolos)
				Arbitraries.strings()
						.withChars("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ!@#$%*_-.".toCharArray())
						.ofMinLength(8)
						.ofMaxLength(40)
						.filter(s -> !s.matches(".*[0-9].*")),

				// sem maiúscula
				Arbitraries.strings()
						.withChars("abcdefghijklmnopqrstuvwxyz0123456789!@#$%*_-.".toCharArray())
						.ofMinLength(8)
						.ofMaxLength(40)
						.filter(s -> !s.matches(".*[A-Z].*")),

				// sem minúscula
				Arbitraries.strings()
						.withChars("ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%*_-.".toCharArray())
						.ofMinLength(8)
						.ofMaxLength(40)
						.filter(s -> !s.matches(".*[a-z].*"))
		);
	}

	@Provide
	public Arbitrary<String> passwordsOver255() {
		// garante que passa nos requisitos (tem lower/upper/digit),
		// mas excede 255 no @Size(max=255)
		String prefix = "aA0";
		return Arbitraries.strings()
				.withChars("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray())
				.ofMinLength(253) // 3 + 253 = 256
				.ofMaxLength(300)
				.map(s -> prefix + s);
	}

	@Provide
	Arbitrary<String> passwordMissingDigit() {
		char[] allowedNoDigit = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ!@#$%*_-.".toCharArray();
		return Combinators.combine(
				Arbitraries.integers().between(8, 40),
				Arbitraries.chars().range('a', 'z'),
				Arbitraries.chars().range('A', 'Z')
		).flatAs((length, lc, uc) -> {
			int remaining = length - 2;
			return Arbitraries.strings()
					.withChars(allowedNoDigit)
					.ofLength(remaining)
					.map(r -> "" + lc + uc + r);
		});
	}

	@Provide
	Arbitrary<String> passwordMissingUpper() {
		char[] allowedNoUpper = "abcdefghijklmnopqrstuvwxyz0123456789!@#$%*_-.".toCharArray();
		return Combinators.combine(
				Arbitraries.integers().between(8, 40),
				Arbitraries.chars().range('a', 'z'),
				Arbitraries.chars().range('0', '9')
		).flatAs((length, lc, dc) -> {
			int remaining = length - 2;
			return Arbitraries.strings()
					.withChars(allowedNoUpper)
					.ofLength(remaining)
					.map(r -> "" + lc + dc + r);
		});
	}

	@Provide
	Arbitrary<String> passwordMissingLower() {
		char[] allowedNoLower = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%*_-.".toCharArray();
		return Combinators.combine(
				Arbitraries.integers().between(8, 40),
				Arbitraries.chars().range('A', 'Z'),
				Arbitraries.chars().range('0', '9')
		).flatAs((length, uc, dc) -> {
			int remaining = length - 2;
			return Arbitraries.strings()
					.withChars(allowedNoLower)
					.ofLength(remaining)
					.map(r -> "" + uc + dc + r);
		});
	}
}