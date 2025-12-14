package com.softKit.softKit_BE.user.api.dto;

import com.softKit.softKit_BE.user.domain.enums.Status;

import net.jqwik.api.*;

import static org.assertj.core.api.Assertions.assertThat;

import com.softKit.softKit_BE.testSupport.BeanValidationSupport;

class UpdateUserStatusRequestTest extends BeanValidationSupport {

	@Provide
	Arbitrary<Status> anyStatus() {
		return Arbitraries.of(Status.values());
	}

	@Property(tries = 50)
	void nonNullStatus_hasNoViolations(@ForAll("anyStatus") Status status) {
		var req = new UpdateUserStatusRequest(status);
		var violations = validate(req);

		assertThat(violations).isEmpty();
	}

	@Example
	void nullStatus_isInvalid() {
		var req = new UpdateUserStatusRequest(null);
		var violations = validate(req);

		assertHasViolation(violations, "status");
	}
}