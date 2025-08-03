package com.softKit.softKit_BE.model.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserVO(
        @NotBlank(message = "Name is required!")
        @Size(max = 150, message = "Name must contain until 150 chars")
        String name,

        @NotBlank(message = "mail is required!")
        @Size(max = 150, message = "Mail must contain until 150 chars")
        String email,

        @Size(max = 20, message = "Phone should have at maximum 20 chars")
        String phone
) {
}
