package com.softKit.softKit_BE.user.api.dto;

import com.softKit.softKit_BE.user.domain.enums.Status;
import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(
		@NotNull
		Status status
) {}
