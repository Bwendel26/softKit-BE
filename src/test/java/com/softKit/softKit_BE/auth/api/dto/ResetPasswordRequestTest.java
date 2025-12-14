package com.softKit.softKit_BE.auth.api.dto;

import net.jqwik.api.*;
import net.jqwik.api.Arbitrary;

import static org.assertj.core.api.Assertions.assertThat;

import com.softKit.softKit_BE.testSupport.PasswordArbitrariesSupport;

class ResetPasswordRequestTest extends PasswordArbitrariesSupport {

	@Provide
	Arbitrary<String> validToken() {
		char[] tokenChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_.".toCharArray();
		return Arbitraries.strings()
				.withChars(tokenChars)
				.ofMinLength(1)
				.ofMaxLength(80);
	}

	@Property(tries = 200)
	void validRequest_hasNoViolations(
			@ForAll("validToken") String token,
			@ForAll("validPasswords") String password
	) {
		var req = new ResetPasswordRequest(token, password, password);
		var violations = validate(req);

		assertThat(violations).isEmpty();
	}

	@Property(tries = 80)
	void blankToken_isInvalid(
			@ForAll("blankStrings") String blank,
			@ForAll("validPasswords") String password
	) {
		var req = new ResetPasswordRequest(blank, password, password);
		var violations = validate(req);

		assertHasViolation(violations, "token");
	}

	@Property(tries = 80)
	void blankNewPassword_isInvalid(
			@ForAll("validToken") String token,
			@ForAll("blankStrings") String blank
	) {
		// deixa confirmNewPassword igual ao newPassword pra não disparar @PasswordsMatch por "mismatch"
		var req = new ResetPasswordRequest(token, blank, blank);
		var violations = validate(req);

		assertHasViolation(violations, "newPassword");
		assertHasViolation(violations, "confirmNewPassword");
	}

	@Property(tries = 120)
	void passwordMissingDigit_violatesPattern(
			@ForAll("validToken") String token,
			@ForAll("passwordMissingDigit") String passwordNoDigit
	) {
		var req = new ResetPasswordRequest(token, passwordNoDigit, passwordNoDigit);
		var violations = validate(req);

		assertHasViolation(violations, "newPassword");
	}

	@Property(tries = 120)
	void passwordMissingUpper_violatesPattern(
			@ForAll("validToken") String token,
			@ForAll("passwordMissingUpper") String passwordNoUpper
	) {
		var req = new ResetPasswordRequest(token, passwordNoUpper, passwordNoUpper);
		var violations = validate(req);

		assertHasViolation(violations, "newPassword");
	}

	@Property(tries = 120)
	void passwordMissingLower_violatesPattern(
			@ForAll("validToken") String token,
			@ForAll("passwordMissingLower") String passwordNoLower
	) {
		var req = new ResetPasswordRequest(token, passwordNoLower, passwordNoLower);
		var violations = validate(req);

		assertHasViolation(violations, "newPassword");
	}

	@Property(tries = 80)
	void blankConfirmNewPassword_isInvalid(
			@ForAll("validToken") String token,
			@ForAll("validPasswords") String password,
			@ForAll("blankStrings") String blank
	) {
		var req = new ResetPasswordRequest(token, password, blank);
		var violations = validate(req);

		// @NotBlank + @PasswordsMatch colocam o erro em confirmNewPassword
		assertHasViolation(violations, "confirmNewPassword");
		assertNoViolation(violations, "token");
	}

	@Property(tries = 120)
	void confirmNewPasswordDifferentFromNewPassword_isInvalid(
			@ForAll("validToken") String token,
			@ForAll("validPasswords") String password,
			@ForAll("validPasswords") String otherPassword
	) {
		Assume.that(!password.equals(otherPassword));

		var req = new ResetPasswordRequest(token, password, otherPassword);
		var violations = validate(req);

		assertHasViolation(violations, "confirmNewPassword");
		assertNoViolation(violations, "token");
		assertNoViolation(violations, "newPassword");
	}

	@Example
	void nullFields_areInvalidDueToNotBlank() {
		var req = new ResetPasswordRequest(null, null, null);
		var violations = validate(req);

		assertHasViolation(violations, "token");
		assertHasViolation(violations, "newPassword");
		assertHasViolation(violations, "confirmNewPassword");
	}
}