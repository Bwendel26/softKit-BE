package com.softKit.softKit_BE.testSupport;

import net.jqwik.api.*;

public abstract class EmailArbitrariesSupport extends BeanValidationSupport {

	@Provide
	public Arbitrary<String> blankStrings() {
		return Arbitraries.of("", " ", "   ", "\t", "\n", "\r\n");
	}

	@Provide
	public Arbitrary<String> validEmailsUpTo150() {
		Arbitrary<String> local = safeLocalPart();
		Arbitrary<String> domain = safeDomain();
		Arbitrary<String> tld = safeTld();

		return Combinators.combine(local, domain, tld)
				.as((l, d, t) -> l + "@" + d + "." + t)
				.filter(e -> e.length() <= 150);
	}

	@Provide
	public Arbitrary<String> invalidEmails() {
		// Evita casos ambíguos tipo "a@b" que podem passar no @Email.
		return Arbitraries.oneOf(
				Arbitraries.of(
						"plainaddress",
						"no-at-symbol.com",
						"a@",
						"@example.com",
						"a@@example.com",
						"a@exa mple.com",
						"a@.com",
						"a@example..com",
						"a..b@example.com",
						".ab@example.com",
						"ab.@example.com",
						"ab@-example.com",
						"ab@example-.com",
						"ab@example.com.",
						"ab@example,com"
				),
				Arbitraries.strings()
						.withChars("abcdefghijklmnopqrstuvwxyz0123456789._-".toCharArray())
						.ofMinLength(1)
						.ofMaxLength(60)
						.filter(s -> !s.contains("@"))
		);
	}

	// ---------- protected helpers para reuso em outros arbitraries ----------

	protected Arbitrary<String> safeLocalPart() {
		char[] firstLast = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
		char[] middle = "abcdefghijklmnopqrstuvwxyz0123456789._%+-".toCharArray();

		Arbitrary<String> one = Arbitraries.strings().withChars(firstLast).ofLength(1);

		Arbitrary<String> many = Combinators.combine(
						Arbitraries.strings().withChars(firstLast).ofLength(1),
						Arbitraries.strings().withChars(middle).ofMinLength(0).ofMaxLength(62),
						Arbitraries.strings().withChars(firstLast).ofLength(1)
				).as((a, mid, b) -> a + mid + b)
				.filter(s -> !s.contains(".."));

		return Arbitraries.oneOf(one, many);
	}

	protected Arbitrary<String> safeDomain() {
		Arbitrary<String> label = safeDomainLabel();
		return Arbitraries.integers().between(1, 3)
				.flatMap(n -> label.list().ofSize(n).map(parts -> String.join(".", parts)));
	}

	protected Arbitrary<String> safeDomainLabel() {
		char[] alnum = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
		char[] mid = "abcdefghijklmnopqrstuvwxyz0123456789-".toCharArray();

		Arbitrary<String> one = Arbitraries.strings().withChars(alnum).ofLength(1);

		Arbitrary<String> many = Combinators.combine(
				Arbitraries.strings().withChars(alnum).ofLength(1),
				Arbitraries.strings().withChars(mid).ofMinLength(0).ofMaxLength(61),
				Arbitraries.strings().withChars(alnum).ofLength(1)
		).as((a, midPart, b) -> a + midPart + b);

		return Arbitraries.oneOf(one, many);
	}

	protected Arbitrary<String> safeTld() {
		return Arbitraries.strings()
				.withChars("abcdefghijklmnopqrstuvwxyz".toCharArray())
				.ofMinLength(2)
				.ofMaxLength(10);
	}

	@Provide
	protected Arbitrary<String> safeEmailLikeNullable() {
		return validEmailsUpTo150().injectNull(0.05);
	}

	@Provide
	protected Arbitrary<String> emailsOver150() {
		// Gera email "bem comportado" (tende a passar no @Email) mas > 150,
		// para garantir falha no @Size(max=150).
		return Combinators.combine(safeLocalPart(), safeDomainLabel(), safeTld())
				.as((local, domainLabel, tld) -> {
					String base = local + "@" + domainLabel + "." + tld;

					int need = 151 - base.length();
					if (need <= 0) need = 1;

					// Estoura o tamanho adicionando labels extras no domínio (<=63 chars por label)
					String extraLabel = "a".repeat(Math.min(63, Math.max(1, need)));
					String domain = domainLabel + "." + extraLabel;

					String email = local + "@" + domain + "." + tld;

					// Garante > 150 mesmo em casos “curtos”
					if (email.length() <= 150) {
						email = email + "a".repeat(151 - email.length());
					}
					return email;
				})
				.filter(e -> e.length() > 150);
	}
}