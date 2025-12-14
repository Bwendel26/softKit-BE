package com.softKit.softKit_BE.user.api.dto;

import com.softKit.softKit_BE.shared.validation.PasswordsMatch;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@PasswordsMatch(message = "New password and confirmation must match")
public record ChangePasswordRequest(

		@NotBlank
		String currentPassword,

		@NotBlank
		@Size(min = 8, max = 255)
		@Pattern(
				regexp = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[a-z]).{8,}$",
				message = "Password must have at least 8 chars, 1 upper, 1 lower, 1 digit"
		)
		String newPassword,

		@NotBlank
		String confirmNewPassword
) {}
