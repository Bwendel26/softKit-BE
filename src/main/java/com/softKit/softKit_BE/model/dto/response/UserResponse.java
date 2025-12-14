package com.softKit.softKit_BE.model.dto.response;

import com.softKit.softKit_BE.model.enums.Role;
import com.softKit.softKit_BE.model.enums.Status;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

public record UserResponse(
		UUID id,
		String fullName,
		String email,
		String phoneE164,
		Role role,
		Status status,
		OffsetDateTime emailVerifiedAt,
		OffsetDateTime lastLoginAt,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt
) {}
