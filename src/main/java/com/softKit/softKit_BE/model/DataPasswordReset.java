package com.softKit.softKit_BE.model;

import jakarta.validation.constraints.NotBlank;

public record DataPasswordReset(@NotBlank String password,
                                @NotBlank String newPassword,
                                @NotBlank String confirmNewPassword) {
}
