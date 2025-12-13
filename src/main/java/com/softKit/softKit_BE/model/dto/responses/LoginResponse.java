package com.softKit.softKit_BE.model.dto.responses;

public record LoginResponse(
		String token,
		String tokenType,
		long expiresIn,
		UserResponse user
) {}
