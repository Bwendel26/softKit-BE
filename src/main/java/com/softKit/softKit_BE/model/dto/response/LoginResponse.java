package com.softKit.softKit_BE.model.dto.response;

public record LoginResponse(
		String token,
		String tokenType,
		long expiresIn,
		UserResponse user
) {}
