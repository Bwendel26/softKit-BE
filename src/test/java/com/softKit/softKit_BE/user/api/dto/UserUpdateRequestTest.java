package com.softKit.softKit_BE.user.api.dto;

import com.softKit.softKit_BE.testSupport.PasswordArbitrariesSupport;
import net.jqwik.api.*;

import static org.assertj.core.api.Assertions.assertThat;

class UserUpdateRequestTest extends PasswordArbitrariesSupport {

	@Provide
	Arbitrary<String> emailLikeUpTo150NonBlank() {
		// Não tem @Email, então só precisa ser não-blank e <=150.
		char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789._%+-@".toCharArray();
		return Arbitraries.strings()
				.withChars(chars)
				.ofMinLength(1)
				.ofMaxLength(150)
				.map(String::trim)
				.filter(s -> !s.isBlank());
	}

	@Provide
	Arbitrary<String> emailOver150() {
		char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789._%+-@".toCharArray();
		return Arbitraries.strings()
				.withChars(chars)
				.ofMinLength(151)
				.ofMaxLength(220)
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
			@ForAll("emailLikeUpTo150NonBlank") String email,
			@ForAll("phoneUpTo20NonBlank") String phone
	) {
		var req = new UserUpdateRequest(fullName, email, phone);
		var violations = validate(req);

		assertThat(violations).isEmpty();
	}

	@Property(tries = 80)
	void blankFullName_isInvalid(
			@ForAll("blankStrings") String blank,
			@ForAll("emailLikeUpTo150NonBlank") String email,
			@ForAll("phoneUpTo20NonBlank") String phone
	) {
		var req = new UserUpdateRequest(blank, email, phone);
		var violations = validate(req);

		assertHasViolation(violations, "fullName");
	}

	@Property(tries = 80)
	void blankEmail_isInvalid(
			@ForAll("validFullNameUpTo200") String fullName,
			@ForAll("blankStrings") String blank,
			@ForAll("phoneUpTo20NonBlank") String phone
	) {
		var req = new UserUpdateRequest(fullName, blank, phone);
		var violations = validate(req);

		assertHasViolation(violations, "email");
	}

	@Property(tries = 80)
	void blankPhone_isInvalid(
			@ForAll("validFullNameUpTo200") String fullName,
			@ForAll("emailLikeUpTo150NonBlank") String email,
			@ForAll("blankStrings") String blank
	) {
		var req = new UserUpdateRequest(fullName, email, blank);
		var violations = validate(req);

		assertHasViolation(violations, "phoneE164");
	}

	@Property(tries = 60)
	void fullNameOver200_isInvalid(
			@ForAll("fullNameOver200") String tooLongName,
			@ForAll("emailLikeUpTo150NonBlank") String email,
			@ForAll("phoneUpTo20NonBlank") String phone
	) {
		var req = new UserUpdateRequest(tooLongName, email, phone);
		var violations = validate(req);

		assertHasViolation(violations, "fullName");
	}

	@Property(tries = 60)
	void emailOver150_isInvalid(
			@ForAll("validFullNameUpTo200") String fullName,
			@ForAll("emailOver150") String tooLongEmail,
			@ForAll("phoneUpTo20NonBlank") String phone
	) {
		var req = new UserUpdateRequest(fullName, tooLongEmail, phone);
		var violations = validate(req);

		assertHasViolation(violations, "email");
	}

	@Property(tries = 60)
	void phoneOver20_isInvalid(
			@ForAll("validFullNameUpTo200") String fullName,
			@ForAll("emailLikeUpTo150NonBlank") String email,
			@ForAll("phoneOver20") String tooLongPhone
	) {
		var req = new UserUpdateRequest(fullName, email, tooLongPhone);
		var violations = validate(req);

		assertHasViolation(violations, "phoneE164");
		assertNoViolation(violations, "fullName");
		assertNoViolation(violations, "email");
	}

	@Example
	void nullFields_areInvalid() {
		var req = new UserUpdateRequest(null, null, null);
		var violations = validate(req);

		assertHasViolation(violations, "fullName");
		assertHasViolation(violations, "email");
		assertHasViolation(violations, "phoneE164");
	}
}