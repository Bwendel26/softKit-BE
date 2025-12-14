package com.softKit.softKit_BE.testSupport;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class BeanValidationSupport {

	protected static final Validator VALIDATOR;

	static {
		try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
			VALIDATOR = factory.getValidator();
		}
	}

	protected <T> Set<ConstraintViolation<T>> validate(T obj) {
		return VALIDATOR.validate(obj);
	}

	protected void assertHasViolation(Set<? extends ConstraintViolation<?>> violations, String field) {
		assertThat(violations)
				.as("Expected violation on '%s' but got: %s", field, violations)
				.anyMatch(v -> v.getPropertyPath().toString().equals(field));
	}

	protected void assertNoViolation(Set<? extends ConstraintViolation<?>> violations, String field) {
		assertThat(violations)
				.as("Expected NO violation on '%s' but got: %s", field, violations)
				.noneMatch(v -> v.getPropertyPath().toString().equals(field));
	}
}