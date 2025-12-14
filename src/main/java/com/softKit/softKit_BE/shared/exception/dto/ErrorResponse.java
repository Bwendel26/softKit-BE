package com.softKit.softKit_BE.shared.exception.dto;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
		String message,
		Map<String, String> errors,
		int status,
		String path,
		Instant timestamp
) {}
