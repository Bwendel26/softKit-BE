package com.softKit.softKit_BE.auth.api.dto;

import net.jqwik.api.*;

import static org.assertj.core.api.Assertions.assertThat;

import com.softKit.softKit_BE.testSupport.EmailArbitrariesSupport;

class ForgotPasswordRequestTest extends EmailArbitrariesSupport {

	@Property(tries = 200)
	void validEmail_upTo150_hasNoViolations(@ForAll("validEmailsUpTo150") String email) {
		var req = new ForgotPasswordRequest(email);
		var violations = validate(req);

		assertThat(violations).isEmpty();
	}

	@Property(tries = 80)
	void blankEmail_isInvalid(@ForAll("blankStrings") String blank) {
		var req = new ForgotPasswordRequest(blank);
		var violations = validate(req);

		assertHasViolation(violations, "email");
	}

	@Property(tries = 200)
	void invalidEmailFormat_isInvalid(@ForAll("invalidEmails") String invalidEmail) {
		var req = new ForgotPasswordRequest(invalidEmail);
		var violations = validate(req);

		assertHasViolation(violations, "email");
	}

	@Property(tries = 60)
	void emailOver150_isInvalid(@ForAll("emailsOver150") String tooLongEmail) {
		var req = new ForgotPasswordRequest(tooLongEmail);
		var violations = validate(req);

		assertHasViolation(violations, "email");
	}

	@Example
	void nullEmail_isInvalid() {
		var req = new ForgotPasswordRequest(null);
		var violations = validate(req);

		assertHasViolation(violations, "email");
	}
}