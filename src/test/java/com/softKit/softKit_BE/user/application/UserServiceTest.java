package com.softKit.softKit_BE.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import com.softKit.softKit_BE.shared.exception.EmailAlreadyInUseException;
import com.softKit.softKit_BE.shared.exception.UserNotFoundException;
import com.softKit.softKit_BE.user.api.dto.ChangePasswordRequest;
import com.softKit.softKit_BE.user.api.dto.UserCreateRequest;
import com.softKit.softKit_BE.user.api.dto.UserResponse;
import com.softKit.softKit_BE.user.api.dto.UserUpdateRequest;
import com.softKit.softKit_BE.user.domain.User;
import com.softKit.softKit_BE.user.domain.enums.Role;
import com.softKit.softKit_BE.user.domain.enums.Status;
import com.softKit.softKit_BE.user.application.mapper.UserMapper;
import com.softKit.softKit_BE.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
	private UserService userService;

	private User user;
	private UserCreateRequest createRequest;
	private UserUpdateRequest updateRequest;

	@BeforeEach
	void setUp() {
		user = new User();
		user.setUsername("john@example.com");
		user.setFullName("John Doe");
		user.setPasswordHash("hashed");

		createRequest = new UserCreateRequest(
				"John Doe",
				"john@example.com",
				"+5511999999999",
				"Password1"
		);

		updateRequest = new UserUpdateRequest(
				"John Updated",
				"john_updated@example.com",
				"+5511888888888"
		);
	}

	@Test
	void getById_shouldReturnUser_whenExists() {
		UUID userId = user.getId();

		UserResponse mapped = new UserResponse(
				userId,
				user.getFullName(),
				user.getEmail(),
				user.getPhoneE164(),
				user.getRole(),
				user.getStatus(),
				user.getEmailVerifiedAt(),
				user.getLastLoginAt(),
				user.getCreatedAt(),
				user.getUpdatedAt()
		);

		when(repository.findById(userId)).thenReturn(Optional.of(user));

		when(mapper.toUserResponse(user)).thenReturn(mapped);

		UserResponse response = userService.getById(userId);

		assertThat(response.id()).isEqualTo(userId);
		assertThat(response.fullName()).isEqualTo("John Doe");
		verify(repository).findById(userId);
		verify(mapper).toUserResponse(user);
	}

	@Test
	void getById_shouldThrow_whenUserDoesNotExist() {
		UUID userId = user.getId();
		when(repository.findById(userId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> userService.getById(userId))
				.isInstanceOf(UserNotFoundException.class);
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
				new UserResponse(UUID.randomUUID(), "John Doe", "john@example.com", null,
						Role.CUSTOMER, Status.ACTIVE, null, null,
						OffsetDateTime.now(), OffsetDateTime.now())
		);
		when(mapper.toUserResponse(user2)).thenReturn(
				new UserResponse(UUID.randomUUID(), "Jane Doe", "jane@example.com", null,
						Role.ADMIN, Status.ACTIVE, null, null,
						OffsetDateTime.now(), OffsetDateTime.now())
		);

		Page<UserResponse> response = userService.getAll(pageable);

		assertEquals(2, response.getTotalElements());
		assertEquals("John Doe", response.getContent().getFirst().fullName());
	}

	@Test
	void create_shouldHashPasswordAndSave_whenEmailNotExists() {
		UUID userId = user.getId();
		when(repository.existsByEmail(createRequest.email())).thenReturn(false);
		when(mapper.fromCreateRequestToEntity(createRequest)).thenReturn(new User());
		when(passwordEncoder.encode(createRequest.password())).thenReturn("ENCODED");
		User saved = new User();
		saved.setUsername(createRequest.email());
		saved.setPasswordHash("ENCODED");
		saved.setFullName(createRequest.fullName());
		saved.setPhoneE164(createRequest.phoneE164());
		when(repository.save(any(User.class))).thenReturn(saved);

		UserResponse mapped = new UserResponse(
				userId,
				saved.getFullName(),
				saved.getEmail(),
				saved.getPhoneE164(),
				saved.getRole(),
				saved.getStatus(),
				saved.getEmailVerifiedAt(),
				saved.getLastLoginAt(),
				saved.getCreatedAt(),
				saved.getUpdatedAt()
		);
		when(mapper.toUserResponse(saved)).thenReturn(mapped);

		UserResponse result = userService.create(createRequest);

		assertThat(result.email()).isEqualTo(createRequest.email());
		verify(repository).existsByEmail(createRequest.email());
		verify(passwordEncoder).encode(createRequest.password());
		verify(repository).save(any(User.class));
	}

	@Test
	void create_shouldThrow_whenEmailAlreadyInUse() {
		when(repository.existsByEmail(createRequest.email())).thenReturn(true);

		assertThatThrownBy(() -> userService.create(createRequest))
				.isInstanceOf(EmailAlreadyInUseException.class);

		verify(repository, never()).save(any());
	}

	@Test
	void update_shouldUpdateUser_whenExistsAndEmailFree() {
		when(repository.findById(user.getId())).thenReturn(Optional.of(user));
		when(repository.existsByEmailAndIdNot(updateRequest.email(), user.getId())).thenReturn(false);

		User updated = new User();
		updated.setUsername(updateRequest.email());
		updated.setFullName(updateRequest.fullName());
		when(repository.save(user)).thenReturn(updated);

		UserResponse mapped = new UserResponse(
				user.getId(),
				updated.getFullName(),
				updated.getEmail(),
				updated.getPhoneE164(),
				updated.getRole(),
				updated.getStatus(),
				updated.getEmailVerifiedAt(),
				updated.getLastLoginAt(),
				updated.getCreatedAt(),
				updated.getUpdatedAt()
		);
		when(mapper.toUserResponse(updated)).thenReturn(mapped);

		UserResponse result = userService.update(user.getId(), updateRequest);

		assertThat(result.fullName()).isEqualTo(updateRequest.fullName());
		verify(mapper).updateEntityFromDto(updateRequest, user);
		verify(repository).save(user);
	}

	@Test
	void update_shouldThrow_whenUserNotFound() {
		when(repository.findById(user.getId())).thenReturn(Optional.empty());

		assertThatThrownBy(() -> userService.update(user.getId(), updateRequest))
				.isInstanceOf(UserNotFoundException.class);
	}

	@Test
	void update_shouldThrow_whenEmailBelongsToAnotherUser() {
		when(repository.findById(user.getId())).thenReturn(Optional.of(user));
		when(repository.existsByEmailAndIdNot(updateRequest.email(), user.getId())).thenReturn(true);

		assertThatThrownBy(() -> userService.update(user.getId(), updateRequest))
				.isInstanceOf(EmailAlreadyInUseException.class);

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

		userService.changePassword(request);

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
				new UserResponse(UUID.randomUUID(), "John Doe", "john@example.com", null,
						Role.CUSTOMER, Status.ACTIVE, null, null,
						OffsetDateTime.now(), OffsetDateTime.now())
		);

		UserResponse response = userService.getCurrentUser();

		assertEquals("john@example.com", response.email());
	}
}