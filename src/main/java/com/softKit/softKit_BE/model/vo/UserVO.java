package com.softKit.softKit_BE.model.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserVO(
        @NotBlank(message = "Full Name is required!")
        @Size(max = 200, message = "Name must contain until 200 chars")
        String fullName,

        @NotBlank(message = "Username is required!")
        @Size(max = 150, message = "Username must contain until 150 chars")
        String username,

        @NotBlank(message = "mail is required!")
        @Size(max = 150, message = "Mail must contain until 150 chars")
        String email,

        @Size(max = 20, message = "Phone should have at maximum 20 chars")
        String phone
) {
}
