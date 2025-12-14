package com.softKit.softKit_BE.shared.validation;

import com.softKit.softKit_BE.auth.api.dto.ResetPasswordRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Objects;

public class ResetPasswordPasswordsMatchValidator
		implements ConstraintValidator<PasswordsMatch, ResetPasswordRequest> {

	@Override
	public boolean isValid(ResetPasswordRequest value,
						   ConstraintValidatorContext context) {
		if (value == null) {
			// deixa @NotNull cuidar disso se for o caso
			return true;
		}

		boolean matches = Objects.equals(value.newPassword(), value.confirmNewPassword());

		if (!matches) {
			// coloca o erro em confirmNewPassword em vez de erro "genérico" no objeto
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(
							context.getDefaultConstraintMessageTemplate()
					)
					.addPropertyNode("confirmNewPassword")
					.addConstraintViolation();
		}

		return matches;
	}
}