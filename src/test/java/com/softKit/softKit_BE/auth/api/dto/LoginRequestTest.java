package com.softKit.softKit_BE.auth.api.dto;

import com.softKit.softKit_BE.testSupport.PasswordArbitrariesSupport;
import net.jqwik.api.*;

import static org.assertj.core.api.Assertions.assertThat;

class LoginRequestTest extends PasswordArbitrariesSupport {

	@Property(tries = 200)
	void validRequest_hasNoViolations(
			@ForAll("validEmailsUpTo150") String email,
			@ForAll("validPasswords") String password
	) {
		var req = new LoginRequest(email, password);
		var violations = validate(req);

		assertThat(violations).isEmpty();
	}

	@Property(tries = 80)
	void blankEmail_isInvalid(
			@ForAll("blankStrings") String blank,
			@ForAll("validPasswords") String password
	) {
		var req = new LoginRequest(blank, password);
		var violations = validate(req);

		assertHasViolation(violations, "email");
		assertNoViolation(violations, "password");
	}

	@Property(tries = 80)
	void invalidEmailFormat_isInvalid(
			@ForAll("invalidEmails") String invalidEmail,
			@ForAll("validPasswords") String password
	) {
		var req = new LoginRequest(invalidEmail, password);
		var violations = validate(req);

		assertHasViolation(violations, "email");
		assertNoViolation(violations, "password");
	}

	@Property(tries = 60)
	void emailOver150_isInvalid(
			@ForAll("emailsOver150") String tooLongEmail,
			@ForAll("validPasswords") String password
	) {
		var req = new LoginRequest(tooLongEmail, password);
		var violations = validate(req);

		assertHasViolation(violations, "email");
		assertNoViolation(violations, "password");
	}

	@Property(tries = 80)
	void blankPassword_isInvalid(
			@ForAll("validEmailsUpTo150") String email,
			@ForAll("blankStrings") String blank
	) {
		var req = new LoginRequest(email, blank);
		var violations = validate(req);

		assertHasViolation(violations, "password");
		assertNoViolation(violations, "email");
	}

	@Example
	void nullFields_areInvalid() {
		var req = new LoginRequest(null, null);
		var violations = validate(req);

		assertHasViolation(violations, "email");
		assertHasViolation(violations, "password");
	}
}