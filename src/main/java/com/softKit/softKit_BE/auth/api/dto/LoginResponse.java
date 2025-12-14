package com.softKit.softKit_BE.auth.api.dto;

import com.softKit.softKit_BE.user.api.dto.UserResponse;

public record LoginResponse(
		String token,
		String tokenType,
		long expiresIn,
		UserResponse user
) {}
