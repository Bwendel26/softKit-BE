package com.softKit.softKit_BE.auth.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
		@NotBlank(message = "Email is required")
		@Email(message = "Invalid email format")
		@Size(max = 150, message = "Email must contain until 150 chars")
		String email,

		@NotBlank(message = "Password is required")
		String password
) {}
