package com.softKit.softKit_BE.auth.api.dto;

import com.softKit.softKit_BE.testSupport.AuthRequestArbitrariesSupport;
import net.jqwik.api.*;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterRequestTest extends AuthRequestArbitrariesSupport {

	@Property(tries = 200)
	void validRegisterRequest_hasNoViolations(
			@ForAll("validFullNameUpTo200_required") String fullName,
			@ForAll("validEmailUpTo150_required") String email,
			@ForAll("phoneNullableUpTo20") String phone,
			@ForAll("password_required_valid") String password
	) {
		var req = new RegisterRequest(fullName, email, phone, password);
		var violations = validate(req);

		assertThat(violations).isEmpty();
	}

	@Property(tries = 80)
	void blankFullName_isInvalid(
			@ForAll("blankStrings") String blank,
			@ForAll("validEmailUpTo150_required") String email,
			@ForAll("phoneNullableUpTo20") String phone,
			@ForAll("password_required_valid") String password
	) {
		var req = new RegisterRequest(blank, email, phone, password);
		var violations = validate(req);

		assertHasViolation(violations, "fullName");
	}

	@Property(tries = 80)
	void blankEmail_isInvalid(
			@ForAll("validFullNameUpTo200_required") String fullName,
			@ForAll("blankStrings") String blank,
			@ForAll("phoneNullableUpTo20") String phone,
			@ForAll("password_required_valid") String password
	) {
		var req = new RegisterRequest(fullName, blank, phone, password);
		var violations = validate(req);

		assertHasViolation(violations, "email");
	}

	@Property(tries = 120)
	void invalidEmailFormat_isInvalid(
			@ForAll("validFullNameUpTo200_required") String fullName,
			@ForAll("invalidEmails") String invalidEmail,
			@ForAll("phoneNullableUpTo20") String phone,
			@ForAll("password_required_valid") String password
	) {
		var req = new RegisterRequest(fullName, invalidEmail, phone, password);
		var violations = validate(req);

		assertHasViolation(violations, "email");
	}

	@Property(tries = 60)
	void fullNameOver200_isInvalid(
			@ForAll("fullNameOver200") String tooLongName,
			@ForAll("validEmailUpTo150_required") String email,
			@ForAll("phoneNullableUpTo20") String phone,
			@ForAll("password_required_valid") String password
	) {
		var req = new RegisterRequest(tooLongName, email, phone, password);
		var violations = validate(req);

		assertHasViolation(violations, "fullName");
	}

	@Property(tries = 60)
	void emailOver150_isInvalid(
			@ForAll("validFullNameUpTo200_required") String fullName,
			@ForAll("emailsOver150") String tooLongEmail,
			@ForAll("phoneNullableUpTo20") String phone,
			@ForAll("password_required_valid") String password
	) {
		var req = new RegisterRequest(fullName, tooLongEmail, phone, password);
		var violations = validate(req);

		assertHasViolation(violations, "email");
	}

	@Property(tries = 60)
	void phoneOver20_isInvalid(
			@ForAll("validFullNameUpTo200_required") String fullName,
			@ForAll("validEmailUpTo150_required") String email,
			@ForAll("phoneOver20") String tooLongPhone,
			@ForAll("password_required_valid") String password
	) {
		var req = new RegisterRequest(fullName, email, tooLongPhone, password);
		var violations = validate(req);

		assertHasViolation(violations, "phoneE164");
		assertNoViolation(violations, "fullName");
		assertNoViolation(violations, "email");
		assertNoViolation(violations, "password");
	}

	@Property(tries = 80)
	void blankPassword_isInvalid(
			@ForAll("validFullNameUpTo200_required") String fullName,
			@ForAll("validEmailUpTo150_required") String email,
			@ForAll("phoneNullableUpTo20") String phone,
			@ForAll("blankStrings") String blankPassword
	) {
		var req = new RegisterRequest(fullName, email, phone, blankPassword);
		var violations = validate(req);

		assertHasViolation(violations, "password");
	}

	@Property(tries = 120)
	void invalidPasswordPattern_isInvalid(
			@ForAll("validFullNameUpTo200_required") String fullName,
			@ForAll("validEmailUpTo150_required") String email,
			@ForAll("phoneNullableUpTo20") String phone,
			@ForAll("invalidPasswords") String invalidPassword
	) {
		var req = new RegisterRequest(fullName, email, phone, invalidPassword);
		var violations = validate(req);

		assertHasViolation(violations, "password");
	}

	@Property(tries = 60)
	void passwordOver255_isInvalid(
			@ForAll("validFullNameUpTo200_required") String fullName,
			@ForAll("validEmailUpTo150_required") String email,
			@ForAll("phoneNullableUpTo20") String phone,
			@ForAll("passwordsOver255") String tooLongPassword
	) {
		var req = new RegisterRequest(fullName, email, phone, tooLongPassword);
		var violations = validate(req);

		assertHasViolation(violations, "password");
	}

	@Example
	void nullPhone_isAllowed() {
		var req = new RegisterRequest("John Doe", "john.doe@example.com", null, "aA0xxxxx");
		var violations = validate(req);

		assertThat(violations).isEmpty();
	}

	@Example
	void emptyPhone_isAllowed_becauseOnlySizeConstraint() {
		var req = new RegisterRequest("John Doe", "john.doe@example.com", "", "aA0xxxxx");
		var violations = validate(req);

		assertThat(violations).isEmpty();
	}

	@Example
	void nullRequiredFields_areInvalid() {
		var req = new RegisterRequest(null, null, null, null);
		var violations = validate(req);

		assertHasViolation(violations, "fullName");
		assertHasViolation(violations, "email");
		assertHasViolation(violations, "password");
		// phoneE164 null é ok
	}
}