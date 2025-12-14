package com.softKit.softKit_BE.testSupport;

import net.jqwik.api.*;

public abstract class UserFieldArbitrariesSupport extends EmailArbitrariesSupport {

	@Provide
	public Arbitrary<String> validFullNameUpTo200() {
		char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ ".toCharArray();
		return Arbitraries.strings()
				.withChars(chars)
				.ofMinLength(1)
				.ofMaxLength(200)
				.map(String::trim)
				.filter(s -> !s.isBlank() && s.length() <= 200);
	}

	@Provide
	public Arbitrary<String> fullNameOver200() {
		char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ ".toCharArray();
		return Arbitraries.strings()
				.withChars(chars)
				.ofMinLength(201)
				.ofMaxLength(260);
	}

	@Provide
	public Arbitrary<String> phoneUpTo20() {
		char[] chars = "+0123456789".toCharArray();
		return Arbitraries.strings()
				.withChars(chars)
				.ofMinLength(0)
				.ofMaxLength(20);
	}

	@Provide
	public Arbitrary<String> phoneOver20() {
		char[] chars = "+0123456789".toCharArray();
		return Arbitraries.strings()
				.withChars(chars)
				.ofMinLength(21)
				.ofMaxLength(40);
	}

	@Provide
	public Arbitrary<String> emailsOver150() {
		// Gera e-mail bem comportado mas >150 para garantir falha do @Size(max=150)
		return Combinators.combine(safeLocalPart(), safeDomainLabel(), safeTld())
				.as((local, domainLabel, tld) -> {
					String base = local + "@" + domainLabel + "." + tld;
					int need = 151 - base.length();
					if (need <= 0) need = 1;

					String extraLabel = "a".repeat(Math.min(63, Math.max(1, need)));
					String domain = domainLabel + "." + extraLabel;

					String email = local + "@" + domain + "." + tld;
					if (email.length() <= 150) {
						email = email + "a".repeat(151 - email.length());
					}
					return email;
				})
				.filter(e -> e.length() > 150);
	}
}
