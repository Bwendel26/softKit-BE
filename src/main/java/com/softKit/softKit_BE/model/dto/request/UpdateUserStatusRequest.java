package com.softKit.softKit_BE.model.dto.request;

import com.softKit.softKit_BE.model.enums.Status;
import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(

		@NotNull
		Status status
) {}
