package com.softKit.softKit_BE.user.api.dto;

import com.softKit.softKit_BE.user.domain.enums.Role;
import com.softKit.softKit_BE.user.domain.enums.Status;

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
