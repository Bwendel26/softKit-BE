package com.softKit.softKit_BE.model.dto.response;

import com.softKit.softKit_BE.model.enums.Role;
import com.softKit.softKit_BE.model.enums.Status;

import java.time.LocalDateTime;

public record UserResponse(
		Long id,
		String fullName,
		String email,
		String phoneE164,
		Role role,
		Status status,
		LocalDateTime emailVerifiedAt,
		LocalDateTime lastLoginAt,
		LocalDateTime createdAt,
		LocalDateTime updatedAt
) {}
