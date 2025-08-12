package com.softKit.softKit_BE.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserCreateDTO(
        @NotBlank @Size(max = 150) String name,
        @Email @NotBlank @Size(max = 150) String email,
        @Size(max = 20) String phone,
        @NotBlank @Size(min = 8, max = 255)
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[a-z]).{8,}$",
                 message = "Password must have at least 8 chars, 1 upper, 1 lower, 1 digit")
        String password
) {}
