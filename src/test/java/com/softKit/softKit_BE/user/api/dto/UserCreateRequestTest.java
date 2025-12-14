package com.softKit.softKit_BE.user.api.dto;

import com.softKit.softKit_BE.testSupport.PasswordArbitrariesSupport;
import net.jqwik.api.*;

import static org.assertj.core.api.Assertions.assertThat;

class UserCreateRequestTest extends PasswordArbitrariesSupport {

	@Provide
	Arbitrary<String> phoneUpTo20NonBlank() {
		// Caso seu support tenha phoneUpTo20() que permite vazio, aqui garantimos NotBlank.
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
			@ForAll("validEmailsUpTo150") String email,
			@ForAll("phoneUpTo20NonBlank") String phone,
			@ForAll("validPasswords") String password
	) {
		var req = new UserCreateRequest(fullName, email, phone, password);
		var violations = validate(req);

		assertThat(violations).isEmpty();
	}

	@Property(tries = 80)
	void blankFullName_isInvalid(
			@ForAll("blankStrings") String blank,
			@ForAll("validEmailsUpTo150") String email,
			@ForAll("phoneUpTo20NonBlank") String phone,
			@ForAll("validPasswords") String password
	) {
		var req = new UserCreateRequest(blank, email, phone, password);
		var violations = validate(req);

		assertHasViolation(violations, "fullName");
	}

	@Property(tries = 80)
	void blankEmail_isInvalid(
			@ForAll("validFullNameUpTo200") String fullName,
			@ForAll("blankStrings") String blank,
			@ForAll("phoneUpTo20NonBlank") String phone,
			@ForAll("validPasswords") String password
	) {
		var req = new UserCreateRequest(fullName, blank, phone, password);
		var violations = validate(req);

		assertHasViolation(violations, "email");
	}

	@Property(tries = 120)
	void invalidEmailFormat_isInvalid(
			@ForAll("validFullNameUpTo200") String fullName,
			@ForAll("invalidEmails") String invalidEmail,
			@ForAll("phoneUpTo20NonBlank") String phone,
			@ForAll("validPasswords") String password
	) {
		var req = new UserCreateRequest(fullName, invalidEmail, phone, password);
		var violations = validate(req);

		assertHasViolation(violations, "email");
	}

	@Property(tries = 60)
	void emailOver150_isInvalid(
			@ForAll("validFullNameUpTo200") String fullName,
			@ForAll("emailsOver150") String tooLongEmail,
			@ForAll("phoneUpTo20NonBlank") String phone,
			@ForAll("validPasswords") String password
	) {
		var req = new UserCreateRequest(fullName, tooLongEmail, phone, password);
		var violations = validate(req);

		assertHasViolation(violations, "email");
	}

	@Property(tries = 60)
	void fullNameOver200_isInvalid(
			@ForAll("fullNameOver200") String tooLongName,
			@ForAll("validEmailsUpTo150") String email,
			@ForAll("phoneUpTo20NonBlank") String phone,
			@ForAll("validPasswords") String password
	) {
		var req = new UserCreateRequest(tooLongName, email, phone, password);
		var violations = validate(req);

		assertHasViolation(violations, "fullName");
	}

	@Property(tries = 80)
	void blankPhone_isInvalid(
			@ForAll("validFullNameUpTo200") String fullName,
			@ForAll("validEmailsUpTo150") String email,
			@ForAll("blankStrings") String blank,
			@ForAll("validPasswords") String password
	) {
		var req = new UserCreateRequest(fullName, email, blank, password);
		var violations = validate(req);

		assertHasViolation(violations, "phoneE164");
	}

	@Property(tries = 60)
	void phoneOver20_isInvalid(
			@ForAll("validFullNameUpTo200") String fullName,
			@ForAll("validEmailsUpTo150") String email,
			@ForAll("phoneOver20") String tooLongPhone,
			@ForAll("validPasswords") String password
	) {
		var req = new UserCreateRequest(fullName, email, tooLongPhone, password);
		var violations = validate(req);

		assertHasViolation(violations, "phoneE164");
	}

	@Property(tries = 80)
	void blankPassword_isInvalid(
			@ForAll("validFullNameUpTo200") String fullName,
			@ForAll("validEmailsUpTo150") String email,
			@ForAll("phoneUpTo20NonBlank") String phone,
			@ForAll("blankStrings") String blank
	) {
		var req = new UserCreateRequest(fullName, email, phone, blank);
		var violations = validate(req);

		assertHasViolation(violations, "password");
	}

	@Property(tries = 120)
	void invalidPasswordPattern_isInvalid(
			@ForAll("validFullNameUpTo200") String fullName,
			@ForAll("validEmailsUpTo150") String email,
			@ForAll("phoneUpTo20NonBlank") String phone,
			@ForAll("invalidPasswords") String invalidPassword
	) {
		var req = new UserCreateRequest(fullName, email, phone, invalidPassword);
		var violations = validate(req);

		assertHasViolation(violations, "password");
	}

	@Property(tries = 60)
	void passwordOver255_isInvalid(
			@ForAll("validFullNameUpTo200") String fullName,
			@ForAll("validEmailsUpTo150") String email,
			@ForAll("phoneUpTo20NonBlank") String phone,
			@ForAll("passwordsOver255") String tooLongPassword
	) {
		var req = new UserCreateRequest(fullName, email, phone, tooLongPassword);
		var violations = validate(req);

		assertHasViolation(violations, "password");
	}

	@Example
	void nullFields_areInvalid() {
		var req = new UserCreateRequest(null, null, null, null);
		var violations = validate(req);

		assertHasViolation(violations, "fullName");
		assertHasViolation(violations, "email");
		assertHasViolation(violations, "phoneE164");
		assertHasViolation(violations, "password");
	}
}