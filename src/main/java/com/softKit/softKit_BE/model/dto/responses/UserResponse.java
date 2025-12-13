package com.softKit.softKit_BE.model.dto.responses;

import com.softKit.softKit_BE.model.Enums.Role;
import com.softKit.softKit_BE.model.Enums.Status;

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
