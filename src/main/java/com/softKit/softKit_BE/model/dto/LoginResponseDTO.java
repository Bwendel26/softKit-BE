package com.softKit.softKit_BE.model.dto;

public record LoginResponseDTO(
        String token, String tokenType, long expiresIn
) {}
