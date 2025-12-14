package com.softKit.softKit_BE.model.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RegisterResponse(
		UUID id,
		String fullName,
	    String email,
	    String phoneE164,
	    OffsetDateTime createdAt,
	    OffsetDateTime updatedAt
) {} // TODO - plan to kill RegisterResponse and use UserResponse instead
