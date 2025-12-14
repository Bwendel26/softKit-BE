package com.softKit.softKit_BE.auth.infrastructure;

import com.softKit.softKit_BE.auth.domain.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
	Optional<PasswordResetToken> findByToken(String token);
}
