package com.softKit.softKit_BE.model.dto.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ForgotPasswordRequest(
		@Email @NotBlank @Size(max = 150)
		String email
) {}
