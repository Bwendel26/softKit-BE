package com.softKit.softKit_BE.model.vo;

import java.time.LocalDateTime;

public record UserResponseVO(Long id,
                             String fullName,
                             String username,
                             String email,
                             String phone,
                             LocalDateTime createdAt,
                             LocalDateTime updatedAt) {
}
