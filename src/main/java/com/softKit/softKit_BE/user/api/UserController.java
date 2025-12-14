package com.softKit.softKit_BE.user.api;


import com.softKit.softKit_BE.user.api.dto.ChangePasswordRequest;
import com.softKit.softKit_BE.user.api.dto.UserResponse;
import com.softKit.softKit_BE.user.application.UserService;
import com.softKit.softKit_BE.user.api.dto.UpdateUserStatusRequest;
import com.softKit.softKit_BE.user.api.dto.UserCreateRequest;
import com.softKit.softKit_BE.user.api.dto.UserSelfUpdateRequest;
import com.softKit.softKit_BE.user.api.dto.UserUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService service;

	public UserController(UserService service) {
		this.service = service;
	}

	// ---------- ADMIN / MANAGEMENT ENDPOINTS ----------
	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
		UserResponse user = service.getById(id);
		return ResponseEntity.ok(user);
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Page<UserResponse>> getAllUsers(Pageable pageable) {
		Page<UserResponse> users = service.getAll(pageable);
		return ResponseEntity.ok(users);
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<UserResponse> createUser(
			@Valid @RequestBody UserCreateRequest userRequest
	) {
		UserResponse newUser = service.create(userRequest);
		return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
	public ResponseEntity<UserResponse> updateUser(
			@PathVariable UUID id,
			@Valid @RequestBody UserUpdateRequest userRequest
	) {
		UserResponse updatedUser = service.update(id, userRequest);
		return ResponseEntity.ok(updatedUser);
	}

	@PatchMapping("/{id}/status")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<UserResponse> updateUserStatus(
			@PathVariable UUID id,
			@Valid @RequestBody UpdateUserStatusRequest request
	) {
		UserResponse updatedUser = service.updateStatus(id, request);
		return ResponseEntity.ok(updatedUser);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
		// soft delete: marca como DELETED (ou INACTIVE, conforme seu enum)
		service.softDelete(id);
		return ResponseEntity.noContent().build();
	}

	// ---------- SELF-SERVICE (USUÁRIO LOGADO) ----------

	@GetMapping("/me")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<UserResponse> getCurrentUser() {
		UserResponse me = service.getCurrentUser();
		return ResponseEntity.ok(me);
	}

	@PutMapping("/me")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<UserResponse> updateMyProfile(
			@Valid @RequestBody UserSelfUpdateRequest request
	) {
		UserResponse updated = service.updateCurrentUser(request);
		return ResponseEntity.ok(updated);
	}

	@PostMapping("/me/change-password")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Void> changePassword(
			@Valid @RequestBody ChangePasswordRequest request
	) {
		service.changePassword(request);
		return ResponseEntity.noContent().build();
	}

}
