package com.softKit.softKit_BE.auth.domain;

import com.softKit.softKit_BE.user.domain.User;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(columnDefinition = "uuid")
	private UUID id;

	@Column(name = "token", nullable = false, unique = true, length = 100)
	private String token;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@Column(name = "expires_at", nullable = false)
	private OffsetDateTime expiresAt;

	@Column(name = "used_at")
	private OffsetDateTime usedAt;

	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@PrePersist
	void onCreate() {
		this.createdAt = OffsetDateTime.now();
	}

	public boolean isExpired() {
		return expiresAt.isBefore(OffsetDateTime.now());
	}

	public boolean isUsed() {
		return usedAt != null;
	}

	public UUID getId() {
		return id;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public OffsetDateTime getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(OffsetDateTime expiresAt) {
		this.expiresAt = expiresAt;
	}

	public OffsetDateTime getUsedAt() {
		return usedAt;
	}

	public void setUsedAt(OffsetDateTime usedAt) {
		this.usedAt = usedAt;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}
}
