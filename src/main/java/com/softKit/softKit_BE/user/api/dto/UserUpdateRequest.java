package com.softKit.softKit_BE.user.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
		@NotBlank @Size(max = 200) String fullName,
		@NotBlank @Size(max = 150) String email,
		@NotBlank @Size(max = 20) String phoneE164
) {}
