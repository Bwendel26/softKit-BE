package com.softKit.softKit_BE.model.dto.requests;

import jakarta.validation.constraints.NotBlank;

public record UserSelfUpdateRequest(

		@NotBlank
		String fullName,

		@NotBlank
		String phoneE164
) {}
