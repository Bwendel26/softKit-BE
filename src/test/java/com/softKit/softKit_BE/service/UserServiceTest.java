package com.softKit.softKit_BE.service;

import static org.junit.jupiter.api.Assertions.*;

import com.softKit.softKit_BE.exception.EmailAlreadyInUseException;
import com.softKit.softKit_BE.exception.UserNotFoundException;
import com.softKit.softKit_BE.model.dto.request.ChangePasswordRequest;
import com.softKit.softKit_BE.model.dto.request.UpdateUserStatusRequest;
import com.softKit.softKit_BE.model.dto.request.UserCreateRequest;
import com.softKit.softKit_BE.model.dto.request.UserSelfUpdateRequest;
import com.softKit.softKit_BE.model.dto.request.UserUpdateRequest;
import com.softKit.softKit_BE.model.dto.response.UserResponse;
import com.softKit.softKit_BE.model.User;
import com.softKit.softKit_BE.model.enums.Role;
import com.softKit.softKit_BE.model.enums.Status;
import com.softKit.softKit_BE.model.mapper.UserMapper;
import com.softKit.softKit_BE.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository repository;

	@Mock
	private UserMapper mapper;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private UserService service;

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void getById_shouldReturnUser_whenExists() {
		User user = new User("John Doe", "john@example.com", "hashed", Role.CUSTOMER);
		user.setStatus(Status.ACTIVE);

		when(repository.findById(1L)).thenReturn(Optional.of(user));
		when(mapper.toUserResponse(user)).thenReturn(
				new UserResponse(1L, "John Doe", "john@example.com", null,
						Role.CUSTOMER, Status.ACTIVE, null, null,
						LocalDateTime.now(), LocalDateTime.now())
		);

		UserResponse response = service.getById(1L);

		assertEquals("john@example.com", response.email());
	}

	@Test
	void getById_shouldThrow_whenNotFound() {
		when(repository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(UserNotFoundException.class,
				() -> service.getById(1L));
	}

	@Test
	void getAll_shouldReturnPagedUsers() {
		User user1 = new User("John Doe", "john@example.com", "hashed", Role.CUSTOMER);
		user1.setStatus(Status.ACTIVE);
		User user2 = new User("Jane Doe", "jane@example.com", "hashed2", Role.ADMIN);
		user2.setStatus(Status.ACTIVE);

		List<User> users = List.of(user1, user2);
		Pageable pageable = PageRequest.of(0, 10, Sort.by("fullName").ascending());
		Page<User> userPage = new PageImpl<>(users, pageable, users.size());

		when(repository.findAll(pageable)).thenReturn(userPage);
		when(mapper.toUserResponse(user1)).thenReturn(
				new UserResponse(1L, "John Doe", "john@example.com", null,
						Role.CUSTOMER, Status.ACTIVE, null, null,
						LocalDateTime.now(), LocalDateTime.now())
		);
		when(mapper.toUserResponse(user2)).thenReturn(
				new UserResponse(2L, "Jane Doe", "jane@example.com", null,
						Role.ADMIN, Status.ACTIVE, null, null,
						LocalDateTime.now(), LocalDateTime.now())
		);

		Page<UserResponse> response = service.getAll(pageable);

		assertEquals(2, response.getTotalElements());
		assertEquals("John Doe", response.getContent().get(0).fullName());
	}

	@Test
	void create_shouldPersistUser_whenEmailNotInUse() {
		UserCreateRequest request = new UserCreateRequest(
				"John Doe",
				"john@example.com",
				"+5511999999999",
				"Password123"
		);

		when(repository.existsByEmail("john@example.com")).thenReturn(false);

		User user = new User("John Doe", "john@example.com", null, Role.CUSTOMER);
		when(mapper.fromCreateRequestToEntity(request)).thenReturn(user);
		when(passwordEncoder.encode("Password123")).thenReturn("hashed");

		User savedUser = user;
		savedUser.setStatus(Status.ACTIVE);
		savedUser.setEmailVerifiedAt(LocalDateTime.now());
		when(repository.save(user)).thenReturn(savedUser);

		when(mapper.toUserResponse(savedUser)).thenReturn(
				new UserResponse(1L, "John Doe", "john@example.com",
						"+5511999999999", Role.CUSTOMER, Status.ACTIVE,
						savedUser.getEmailVerifiedAt(), null,
						LocalDateTime.now(), LocalDateTime.now())
		);

		UserResponse response = service.create(request);

		assertEquals("john@example.com", response.email());
		verify(repository).existsByEmail("john@example.com");
		verify(repository).save(user);
	}

	@Test
	void create_shouldThrow_whenEmailAlreadyInUse() {
		UserCreateRequest request = new UserCreateRequest(
				"John Doe",
				"john@example.com",
				"Password123",
				"+5511999999999"
		);

		when(repository.existsByEmail("john@example.com")).thenReturn(true);

		assertThrows(EmailAlreadyInUseException.class,
				() -> service.create(request));

		verify(repository, never()).save(any());
	}

	@Test
	void changePassword_shouldUpdatePassword_whenCurrentMatchesAndConfirmOk() {
		User user = new User("John Doe", "john@example.com", "old-hash", Role.CUSTOMER);
		user.setStatus(Status.ACTIVE);

		var auth = new UsernamePasswordAuthenticationToken(
				user, null, user.getAuthorities()
		);
		SecurityContextHolder.getContext().setAuthentication(auth);

		ChangePasswordRequest request = new ChangePasswordRequest(
				"OldPass123",
				"NewPass123",
				"NewPass123"
		);

		when(passwordEncoder.matches("OldPass123", "old-hash")).thenReturn(true);
		when(passwordEncoder.encode("NewPass123")).thenReturn("new-hash");

		service.changePassword(request);

		assertEquals("new-hash", user.getPasswordHash());
		verify(repository).save(user);
	}

	@Test
	void getCurrentUser_shouldReturnLoggedUser() {
		User user = new User("John Doe", "john@example.com", "hash", Role.CUSTOMER);
		user.setStatus(Status.ACTIVE);

		var auth = new UsernamePasswordAuthenticationToken(
				user, null, user.getAuthorities()
		);
		SecurityContextHolder.getContext().setAuthentication(auth);

		when(mapper.toUserResponse(user)).thenReturn(
				new UserResponse(1L, "John Doe", "john@example.com", null,
						Role.CUSTOMER, Status.ACTIVE, null, null,
						LocalDateTime.now(), LocalDateTime.now())
		);

		UserResponse response = service.getCurrentUser();

		assertEquals("john@example.com", response.email());
	}
}