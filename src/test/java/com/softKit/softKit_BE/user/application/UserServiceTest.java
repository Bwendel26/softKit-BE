package com.softKit.softKit_BE.user.application;

import com.softKit.softKit_BE.shared.exception.EmailAlreadyInUseException;
import com.softKit.softKit_BE.shared.exception.UserNotFoundException;
import com.softKit.softKit_BE.user.api.dto.*;
import com.softKit.softKit_BE.user.application.mapper.UserMapper;
import com.softKit.softKit_BE.user.domain.User;
import com.softKit.softKit_BE.user.domain.enums.Role;
import com.softKit.softKit_BE.user.domain.enums.Status;
import com.softKit.softKit_BE.user.infrastructure.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
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
		authenticate(user);

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
		authenticate(user);

		when(mapper.toUserResponse(user)).thenReturn(
				new UserResponse(UUID.randomUUID(), "John Doe", "john@example.com", null,
						Role.CUSTOMER, Status.ACTIVE, null, null,
						OffsetDateTime.now(), OffsetDateTime.now())
		);

		UserResponse response = userService.getCurrentUser();

		assertEquals("john@example.com", response.email());
	}

	@Test
	void create_shouldSetStatusActiveAndEncodedPassword_beforeSaving() {
		when(repository.existsByEmail(createRequest.email())).thenReturn(false);

		User entity = new User();
		when(mapper.fromCreateRequestToEntity(createRequest)).thenReturn(entity);

		when(passwordEncoder.encode(createRequest.password())).thenReturn("ENCODED");

		User saved = new User();
		when(repository.save(any(User.class))).thenReturn(saved);
		when(mapper.toUserResponse(saved)).thenReturn(
				new UserResponse(UUID.randomUUID(), "John Doe", "john@example.com", null,
						Role.CUSTOMER, Status.ACTIVE, null, null,
						OffsetDateTime.now(), OffsetDateTime.now())
		);

		userService.create(createRequest);

		ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
		verify(repository).save(captor.capture());

		User toSave = captor.getValue();
		assertThat(toSave.getPasswordHash()).isEqualTo("ENCODED");
		assertThat(toSave.getStatus()).isEqualTo(Status.ACTIVE);
	}

	@Test
	void updateStatus_shouldUpdateAndReturnMappedUser() {
		UUID id = user.getId();
		user.setStatus(Status.ACTIVE);

		when(repository.findById(id)).thenReturn(Optional.of(user));
		when(repository.save(user)).thenReturn(user);

		when(mapper.toUserResponse(user)).thenReturn(
				new UserResponse(UUID.randomUUID(), "John Doe", "john@example.com", null,
						Role.CUSTOMER, Status.DISABLED, null, null,
						OffsetDateTime.now(), OffsetDateTime.now())
		);

		UpdateUserStatusRequest req = new UpdateUserStatusRequest(Status.DISABLED);

		UserResponse response = userService.updateStatus(id, req);

		assertThat(user.getStatus()).isEqualTo(Status.DISABLED);
		assertThat(response.status()).isEqualTo(Status.DISABLED);
		verify(repository).save(user);
	}

	@Test
	void updateStatus_shouldThrow_whenUserNotFound() {
		UUID id = user.getId();
		when(repository.findById(id)).thenReturn(Optional.empty());

		UpdateUserStatusRequest req = new UpdateUserStatusRequest(Status.DISABLED);

		assertThatThrownBy(() -> userService.updateStatus(id, req))
				.isInstanceOf(UserNotFoundException.class);

		verify(repository, never()).save(any());
	}

	@Test
	void softDelete_shouldDisableUserAndSave() {
		UUID id = user.getId();
		user.setStatus(Status.ACTIVE);

		when(repository.findById(id)).thenReturn(Optional.of(user));
		when(repository.save(user)).thenReturn(user);

		userService.softDelete(id);

		assertThat(user.getStatus()).isEqualTo(Status.DISABLED);
		verify(repository).save(user);
	}

	@Test
	void softDelete_shouldThrow_whenUserNotFound() {
		UUID id = user.getId();
		when(repository.findById(id)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> userService.softDelete(id))
				.isInstanceOf(UserNotFoundException.class);

		verify(repository, never()).save(any());
	}

	@Test
	void updateCurrentUser_shouldUpdateAndReturnMappedUser() {
		User logged = new User("John Doe", "john@example.com", "hash", Role.CUSTOMER);
		logged.setStatus(Status.ACTIVE);
		authenticate(logged);

		UserSelfUpdateRequest dto = mock(UserSelfUpdateRequest.class);

		when(repository.save(logged)).thenReturn(logged);
		when(mapper.toUserResponse(logged)).thenReturn(
				new UserResponse(UUID.randomUUID(), "John Doe", "john@example.com", null,
						Role.CUSTOMER, Status.ACTIVE, null, null,
						OffsetDateTime.now(), OffsetDateTime.now())
		);

		UserResponse response = userService.updateCurrentUser(dto);

		assertThat(response.email()).isEqualTo("john@example.com");
		verify(mapper).updateEntityFromSelfDto(dto, logged);
		verify(repository).save(logged);
	}

	@Test
	void getCurrentUser_shouldThrow_whenNoAuthentication() {
		SecurityContextHolder.clearContext();

		assertThatThrownBy(() -> userService.getCurrentUser())
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("No authenticated user");
	}

	@Test
	void getCurrentUser_shouldThrow_whenPrincipalIsNotUser() {
		Authentication auth = new UsernamePasswordAuthenticationToken("not-a-user", null, List.of());
		SecurityContextHolder.getContext().setAuthentication(auth);

		assertThatThrownBy(() -> userService.getCurrentUser())
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("No authenticated user");
	}

	@Test
	void changePassword_shouldThrow_whenCurrentPasswordIsIncorrect() {
		User logged = new User("John Doe", "john@example.com", "old-hash", Role.CUSTOMER);
		logged.setStatus(Status.ACTIVE);
		authenticate(logged);

		ChangePasswordRequest req = new ChangePasswordRequest(
				"WrongOld",
				"NewPass123",
				"NewPass123"
		);

		when(passwordEncoder.matches("WrongOld", "old-hash")).thenReturn(false);

		assertThatThrownBy(() -> userService.changePassword(req))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Current password is incorrect");

		verify(repository, never()).save(any());
		verify(passwordEncoder, never()).encode(any());
	}

	@Test
	void changePassword_shouldThrow_whenNewPasswordConfirmationDoesNotMatch() {
		User logged = new User("John Doe", "john@example.com", "old-hash", Role.CUSTOMER);
		logged.setStatus(Status.ACTIVE);
		authenticate(logged);

		ChangePasswordRequest req = new ChangePasswordRequest(
				"OldPass123",
				"NewPass123",
				"Different123"
		);

		when(passwordEncoder.matches("OldPass123", "old-hash")).thenReturn(true);

		assertThatThrownBy(() -> userService.changePassword(req))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("New password and confirmation do not match");

		verify(repository, never()).save(any());
		verify(passwordEncoder, never()).encode(any());
	}

	@Test
	void loadUserByUsername_shouldReturnUserDetails_whenFound() {
		User found = new User("John Doe", "john@example.com", "hash", Role.CUSTOMER);

		when(repository.findByEmailIgnoreCase("JoHn@Example.com")).thenReturn(Optional.of(found));

		UserDetails loaded = userService.loadUserByUsername("JoHn@Example.com");

		assertThat(loaded).isSameAs(found);
		verify(repository).findByEmailIgnoreCase("JoHn@Example.com");
	}

	@Test
	void loadUserByUsername_shouldThrowUsernameNotFound_whenNotFound() {
		when(repository.findByEmailIgnoreCase("missing@example.com")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> userService.loadUserByUsername("missing@example.com"))
				.isInstanceOf(UsernameNotFoundException.class)
				.hasMessageContaining("User not found with email");
	}

	// =========================
	// Helper
	// =========================
	private void authenticate(User user) {
		var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(auth);
	}
}