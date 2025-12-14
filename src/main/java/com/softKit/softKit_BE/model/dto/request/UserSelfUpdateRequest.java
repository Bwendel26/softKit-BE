package com.softKit.softKit_BE.model.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UserSelfUpdateRequest(

		@NotBlank
		String fullName,

		@NotBlank
		String phoneE164
) {}
