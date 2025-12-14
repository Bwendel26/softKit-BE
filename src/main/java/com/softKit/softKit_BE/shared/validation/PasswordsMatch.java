package com.softKit.softKit_BE.shared.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {
		ResetPasswordPasswordsMatchValidator.class,
		ChangePasswordPasswordsMatchValidator.class
})
public @interface PasswordsMatch {

	String message() default "New password and confirmation do not match";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
