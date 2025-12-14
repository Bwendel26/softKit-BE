package com.softKit.softKit_BE.model.dto.response;

import java.time.LocalDateTime;

public record RegisterResponse(
		Long id,
		String fullName,
	    String email,
	    String phoneE164,
	    LocalDateTime createdAt,
	    LocalDateTime updatedAt
) {} // TODO - plan to kill RegisterResponse and use UserResponse instead
