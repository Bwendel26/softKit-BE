package com.softKit.softKit_BE.user.api.dto;

import com.softKit.softKit_BE.testSupport.PasswordArbitrariesSupport;
import jakarta.validation.ConstraintViolation;
import net.jqwik.api.*;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ChangePasswordRequestTest extends PasswordArbitrariesSupport {

	private static final String PASSWORDS_MATCH_MSG = "New password and confirmation must match";

	@Provide
	Arbitrary<String> nonBlankAny() {
		char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%*_-.".toCharArray();
		return Arbitraries.strings()
				.withChars(chars)
				.ofMinLength(1)
				.ofMaxLength(120)
				.map(String::trim)
				.filter(s -> !s.isBlank());
	}

	private static void assertHasViolationWithFieldAndMessage(
			Set<? extends ConstraintViolation<?>> violations,
			String field,
			String message
	) {
		assertThat(violations)
				.as("Expected violation on '%s' with message '%s' but got: %s", field, message, violations)
				.anyMatch(v -> v.getPropertyPath().toString().equals(field) && v.getMessage().equals(message));
	}

	@Property(tries = 200)
	void validRequest_hasNoViolations(
			@ForAll("nonBlankAny") String currentPassword,
			@ForAll("validPasswords") String newPassword
	) {
		var req = new ChangePasswordRequest(currentPassword, newPassword, newPassword);
		var violations = validate(req);

		assertThat(violations).isEmpty();
	}

	@Property(tries = 120)
	void passwordsDoNotMatch_isInvalid_onConfirmField(
			@ForAll("nonBlankAny") String currentPassword,
			@ForAll("validPasswords") String newPassword,
			@ForAll("validPasswords") String confirmNewPassword
	) {
		Assume.that(newPassword != null && confirmNewPassword != null);
		Assume.that(!newPassword.equals(confirmNewPassword));

		var req = new ChangePasswordRequest(currentPassword, newPassword, confirmNewPassword);
		var violations = validate(req);

		assertHasViolationWithFieldAndMessage(violations, "confirmNewPassword", PASSWORDS_MATCH_MSG);
		assertNoViolation(violations, "currentPassword");
		assertNoViolation(violations, "newPassword");
	}

	@Property(tries = 80)
	void blankCurrentPassword_isInvalid(
			@ForAll("blankStrings") String blank,
			@ForAll("validPasswords") String newPassword
	) {
		var req = new ChangePasswordRequest(blank, newPassword, newPassword);
		var violations = validate(req);

		assertHasViolation(violations, "currentPassword");
	}

	@Property(tries = 80)
	void blankNewPassword_isInvalid(
			@ForAll("nonBlankAny") String currentPassword,
			@ForAll("blankStrings") String blank
	) {
		// mantém match (blank == blank) pra não “poluir” com PasswordsMatch
		var req = new ChangePasswordRequest(currentPassword, blank, blank);
		var violations = validate(req);

		assertHasViolation(violations, "newPassword");
	}

	@Property(tries = 120)
	void invalidNewPasswordPattern_isInvalid(
			@ForAll("nonBlankAny") String currentPassword,
			@ForAll("invalidPasswords") String invalidNewPassword
	) {
		var req = new ChangePasswordRequest(currentPassword, invalidNewPassword, invalidNewPassword);
		var violations = validate(req);

		assertHasViolation(violations, "newPassword");
	}

	@Property(tries = 60)
	void newPasswordOver255_isInvalid(
			@ForAll("nonBlankAny") String currentPassword,
			@ForAll("passwordsOver255") String tooLongPassword
	) {
		var req = new ChangePasswordRequest(currentPassword, tooLongPassword, tooLongPassword);
		var violations = validate(req);

		assertHasViolation(violations, "newPassword");
	}

	@Property(tries = 80)
	void blankConfirmNewPassword_isInvalid(
			@ForAll("nonBlankAny") String currentPassword,
			@ForAll("validPasswords") String newPassword,
			@ForAll("blankStrings") String blank
	) {
		var req = new ChangePasswordRequest(currentPassword, newPassword, blank);
		var violations = validate(req);

		assertHasViolation(violations, "confirmNewPassword");
	}

	@Example
	void nullFields_areInvalid() {
		var req = new ChangePasswordRequest(null, null, null);
		var violations = validate(req);

		assertHasViolation(violations, "currentPassword");
		assertHasViolation(violations, "newPassword");
		assertHasViolation(violations, "confirmNewPassword");
	}
}