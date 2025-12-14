package com.softKit.softKit_BE.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.softKit.softKit_BE.model.enums.Role;
import com.softKit.softKit_BE.model.enums.Status;
import jakarta.persistence.*;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User implements UserDetails, Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(columnDefinition = "uuid")
	private UUID id;

    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

	@Column(name = "email_verified_at")
	private OffsetDateTime emailVerifiedAt;

    @Column(name = "phone_e164", length = 20)
    private String phoneE164;

	@Column(name = "password_hash", nullable = false, length = 255)
	private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
	private Role role = Role.CUSTOMER;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private Status status = Status.PENDING;

	@Column(name = "failed_login_attempts", nullable = false)
	private int failedLoginAttempts = 0;

	@Column(name = "locked_until")
	private OffsetDateTime lockedUntil;

	@Column(name = "last_login_at")
	private OffsetDateTime lastLoginAt;

	@JsonIgnore
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @JsonIgnore
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    public User() {}

    public User(String fullName, String email, String passwordHash, Role role) {
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    // Lifecycle
    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
	}

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
		return lockedUntil == null || lockedUntil.isBefore(OffsetDateTime.now());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
		return status == Status.ACTIVE;
    }

    public UUID getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setUsername(String username) {
        this.email = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

	public String getPasswordHash() {
		return passwordHash;
	}

    public void setPasswordHash(String password) {
        this.passwordHash = password;
    }

    public String getPhoneE164() {
        return phoneE164;
    }

    public void setPhoneE164(String phoneE164) {
        this.phoneE164 = phoneE164;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public int getFailedLoginAttempts() {
		return failedLoginAttempts;
	}

	public void setFailedLoginAttempts(int failedLoginAttempts) {
		this.failedLoginAttempts = failedLoginAttempts;
	}

	public OffsetDateTime getLockedUntil() {
		return lockedUntil;
	}

	public void setLockedUntil(OffsetDateTime lockedUntil) {
		this.lockedUntil = lockedUntil;
	}

	public OffsetDateTime getLastLoginAt() {
		return lastLoginAt;
	}

	public void setLastLoginAt(OffsetDateTime lastLoginAt) {
		this.lastLoginAt = lastLoginAt;
	}

	public OffsetDateTime getEmailVerifiedAt() {
		return emailVerifiedAt;
	}

	public void setEmailVerifiedAt(OffsetDateTime emailVerifiedAt) {
		this.emailVerifiedAt = emailVerifiedAt;
	}

	public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void changePassword(String encryptedPassword) {
        this.passwordHash = encryptedPassword;
    }

    @Override
    public String toString() {
        return "User{" +
                " fullName= " + getFullName() + '\'' +
                ", email= " + getEmail() + '\'' +
                ", phone= " + getPhoneE164() + '\'' +
                ", createdAt= " + getCreatedAt() + '\'' +
                ", updatedAt= " + getUpdatedAt() +
                '}';
    }
}
