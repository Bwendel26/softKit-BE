package com.softKit.softKit_BE.model.vo;

import java.time.LocalDateTime;

public record UserResponseVO(Long id,
                             String fullName,
                             String email,
                             String phoneE164,
                             LocalDateTime createdAt,
                             LocalDateTime updatedAt) {
}
