package com.softKit.softKit_BE.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
        @NotBlank @Size(max = 200) String fullName,
        @NotBlank @Email @Size(max = 150) String email,
		@NotBlank @Size(max = 20) String phoneE164,
        @NotBlank @Size(min = 8, max = 255)
		@Pattern(regexp = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[a-z]).{8,}$",
                 message = "Password must have at least 8 chars, 1 upper, 1 lower, 1 digit")
        String password
) {}
