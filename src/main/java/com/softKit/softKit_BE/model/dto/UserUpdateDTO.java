package com.softKit.softKit_BE.model.dto;

import jakarta.validation.constraints.Size;

public record UserUpdateDTO(
        @Size(max = 200) String fullName,
        @Size(max = 150) String email,
        @Size(max = 20) String phone
) {}
