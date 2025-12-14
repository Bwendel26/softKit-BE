package com.softKit.softKit_BE.user.api.dto;

import com.softKit.softKit_BE.testSupport.UserFieldArbitrariesSupport;
import net.jqwik.api.*;

import static org.assertj.core.api.Assertions.assertThat;

class UserRequestTest extends UserFieldArbitrariesSupport {

	@Property(tries = 200)
	void validUserRequest_hasNoViolations(
			@ForAll("validFullNameUpTo200") String fullName,
			@ForAll("validEmailsUpTo150") String email,
			@ForAll("phoneUpTo20") String phone
	) {
		var req = new UserRequest(fullName, email, phone);
		var violations = validate(req);

		assertThat(violations).isEmpty();
	}

	@Property(tries = 80)
	void blankFullName_isInvalid(
			@ForAll("blankStrings") String blank,
			@ForAll("validEmailsUpTo150") String email,
			@ForAll("phoneUpTo20") String phone
	) {
		var req = new UserRequest(blank, email, phone);
		var violations = validate(req);

		assertHasViolation(violations, "fullName");
		assertNoViolation(violations, "email");
	}

	@Property(tries = 80)
	void blankEmail_isInvalid(
			@ForAll("validFullNameUpTo200") String fullName,
			@ForAll("blankStrings") String blank,
			@ForAll("phoneUpTo20") String phone
	) {
		var req = new UserRequest(fullName, blank, phone);
		var violations = validate(req);

		assertHasViolation(violations, "email");
		assertNoViolation(violations, "fullName");
	}

	@Property(tries = 120)
	void invalidEmailFormat_isInvalid(
			@ForAll("validFullNameUpTo200") String fullName,
			@ForAll("invalidEmails") String invalidEmail,
			@ForAll("phoneUpTo20") String phone
	) {
		var req = new UserRequest(fullName, invalidEmail, phone);
		var violations = validate(req);

		assertHasViolation(violations, "email");
	}

	@Property(tries = 60)
	void fullNameOver200_isInvalid(
			@ForAll("fullNameOver200") String tooLongName,
			@ForAll("validEmailsUpTo150") String email,
			@ForAll("phoneUpTo20") String phone
	) {
		var req = new UserRequest(tooLongName, email, phone);
		var violations = validate(req);

		assertHasViolation(violations, "fullName");
	}

	@Property(tries = 60)
	void emailOver150_isInvalid(
			@ForAll("validFullNameUpTo200") String fullName,
			@ForAll("emailsOver150") String tooLongEmail,
			@ForAll("phoneUpTo20") String phone
	) {
		var req = new UserRequest(fullName, tooLongEmail, phone);
		var violations = validate(req);

		assertHasViolation(violations, "email");
	}

	@Property(tries = 60)
	void phoneOver20_isInvalid(
			@ForAll("validFullNameUpTo200") String fullName,
			@ForAll("validEmailsUpTo150") String email,
			@ForAll("phoneOver20") String tooLongPhone
	) {
		var req = new UserRequest(fullName, email, tooLongPhone);
		var violations = validate(req);

		assertHasViolation(violations, "phoneE164");
		assertNoViolation(violations, "fullName");
		assertNoViolation(violations, "email");
	}

	@Example
	void nullPhone_isAllowed() {
		var req = new UserRequest("John Doe", "john.doe@example.com", null);
		var violations = validate(req);

		assertThat(violations).isEmpty();
	}

	@Example
	void nullRequiredFields_areInvalid() {
		var req = new UserRequest(null, null, null);
		var violations = validate(req);

		assertHasViolation(violations, "fullName");
		assertHasViolation(violations, "email");
		// phoneE164 null é ok
	}
}