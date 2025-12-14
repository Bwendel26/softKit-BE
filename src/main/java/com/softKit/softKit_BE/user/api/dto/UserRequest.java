package com.softKit.softKit_BE.user.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequest(
		@NotBlank(message = "Full Name is required!")
		@Size(max = 200, message = "Name must contain until 200 chars")
		String fullName,

		@NotBlank(message = "Email is required!")
		@Email(message = "Invalid email format")
		@Size(max = 150, message = "Email must contain until 150 chars")
		String email,

		@Size(max = 20, message = "Phone should have at maximum 20 chars")
		String phoneE164
) { }
