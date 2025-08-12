package com.softKit.softKit_BE.model.vo;

import java.time.LocalDateTime;

public record UserResponseVO(Long id, String name, String email, String phone, LocalDateTime createdAt) {
}
