package com.softKit.softKit_BE.user.api.dto;

import com.softKit.softKit_BE.testSupport.BeanValidationSupport;
import net.jqwik.api.*;

import static org.assertj.core.api.Assertions.assertThat;

class UserSelfUpdateRequestTest extends BeanValidationSupport {

	@Provide
	Arbitrary<String> blankStrings() {
		return Arbitraries.of("", " ", "   ", "\t", "\n", "\r\n");
	}

	@Provide
	Arbitrary<String> validFullNameUpTo200() {
		char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ ".toCharArray();
		return Arbitraries.strings()
				.withChars(chars)
				.ofMinLength(1)
				.ofMaxLength(200)
				.map(String::trim)
				.filter(s -> !s.isBlank());
	}

	@Provide
	Arbitrary<String> phoneUpTo20NonBlank() {
		char[] chars = "+0123456789".toCharArray();
		return Arbitraries.strings()
				.withChars(chars)
				.ofMinLength(1)
				.ofMaxLength(20)
				.map(String::trim)
				.filter(s -> !s.isBlank());
	}

	@Property(tries = 200)
	void validRequest_hasNoViolations(
			@ForAll("validFullNameUpTo200") String fullName,
			@ForAll("phoneUpTo20NonBlank") String phone
	) {
		var req = new UserSelfUpdateRequest(fullName, phone);
		var violations = validate(req);

		assertThat(violations).isEmpty();
	}

	@Property(tries = 80)
	void blankFullName_isInvalid(
			@ForAll("blankStrings") String blank,
			@ForAll("phoneUpTo20NonBlank") String phone
	) {
		var req = new UserSelfUpdateRequest(blank, phone);
		var violations = validate(req);

		assertHasViolation(violations, "fullName");
		assertNoViolation(violations, "phoneE164");
	}

	@Property(tries = 80)
	void blankPhone_isInvalid(
			@ForAll("validFullNameUpTo200") String fullName,
			@ForAll("blankStrings") String blank
	) {
		var req = new UserSelfUpdateRequest(fullName, blank);
		var violations = validate(req);

		assertHasViolation(violations, "phoneE164");
		assertNoViolation(violations, "fullName");
	}

	@Example
	void nullFields_areInvalid() {
		var req = new UserSelfUpdateRequest(null, null);
		var violations = validate(req);

		assertHasViolation(violations, "fullName");
		assertHasViolation(violations, "phoneE164");
	}
}