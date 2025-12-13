package com.softKit.softKit_BE.model.dto.requests;

import com.softKit.softKit_BE.model.Enums.Status;
import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(

		@NotNull
		Status status
) {}
