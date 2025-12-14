package com.softKit.softKit_BE.service;

import com.softKit.softKit_BE.exception.EmailAlreadyInUseException;
import com.softKit.softKit_BE.exception.UserNotFoundException;
import com.softKit.softKit_BE.model.enums.Status;
import com.softKit.softKit_BE.model.User;
import com.softKit.softKit_BE.model.dto.request.*;
import com.softKit.softKit_BE.model.dto.response.UserResponse;
import com.softKit.softKit_BE.model.mapper.UserMapper;
import com.softKit.softKit_BE.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository repository;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repository,
                       UserMapper mapper,
                       PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse getById(UUID id) {

        User user = (User) repository.findById(id)
				.orElseThrow(() -> new UserNotFoundException(id));

        return mapper.toUserResponse(user);
    }

    public Page<UserResponse> getAll(Pageable pageable) {
        return repository.findAll(pageable)
				.map(mapper::toUserResponse);
    }

	// ---------- COMMANDS (ADMIN / MANAGEMENT) ----------

    public UserResponse create(UserCreateRequest userRequest) {

        if (repository.existsByEmail(userRequest.email())) {
            throw new EmailAlreadyInUseException(userRequest.email());
        }

        User user = mapper.fromCreateRequestToEntity(userRequest);
//        TODO: CREATE A SOLUTION TO WHEN THE USER RECEIVES AN ACCESS HE MUST HAVE TO CHANGE THE PASSWORD - COMMENTED CODE IS GENERATING A NEW RANDOM PASSWORD
//        String firstPassword = UUID.randomUUID().toString().substring(0, 8);
//        System.out.println("Generated PASSWORD: " + firstPassword);

        String hashedPassword = passwordEncoder.encode(userRequest.password());
        user.setPasswordHash(hashedPassword);

		// se quiser que o usuário criado pelo admin já possa logar
		// verificar flow: register -> ativar por link no email ou register -> ativo
		user.setStatus(Status.ACTIVE);

        return mapper.toUserResponse(repository.save(user));
    }

    public UserResponse update(UUID id, UserUpdateRequest dto) {

		User user = (User) repository.findById(id)
				.orElseThrow(() -> new UserNotFoundException(id));

		if (repository.existsByEmailAndIdNot(dto.email(), id)) {
			throw new EmailAlreadyInUseException(dto.email());
		}

        mapper.updateEntityFromDto(dto, user);

        return mapper.toUserResponse(repository.save(user));
    }

	public UserResponse updateStatus(UUID id, UpdateUserStatusRequest request) {

		User user = (User) repository.findById(id)
				.orElseThrow(() -> new UserNotFoundException(id));

		user.setStatus(request.status());

		return mapper.toUserResponse(repository.save(user));
	}

	public void softDelete(UUID id) {
		User user = (User) repository.findById(id)
				.orElseThrow(() -> new UserNotFoundException(id));

		user.setStatus(Status.DISABLED);
		repository.save(user);
	}

	// ---------- SELF-SERVICE (usuário logado) ----------
	public UserResponse getCurrentUser() {
		return mapper.toUserResponse(getAuthenticatedUser());
	}

	public UserResponse updateCurrentUser(UserSelfUpdateRequest dto) {
		User user = getAuthenticatedUser();

		mapper.updateEntityFromSelfDto(dto, user);

		return mapper.toUserResponse(repository.save(user));
	}

	public void changePassword(ChangePasswordRequest data) {
		User loggedIn = getAuthenticatedUser();

		if (!passwordEncoder.matches(data.currentPassword(), loggedIn.getPassword())) {
			throw new IllegalArgumentException("Current password is incorrect");
		}

		if (!data.newPassword().equals(data.confirmNewPassword())) {
			throw new IllegalArgumentException("New password and confirmation do not match");
		}

		String encryptedPassword = passwordEncoder.encode(data.newPassword());
		loggedIn.changePassword(encryptedPassword);

		repository.save(loggedIn);
	}

	// ---------- SPRING SECURITY (UserDetailsService) ----------
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Optional<User> optionalUser = repository.findByEmailIgnoreCase(username);

		return optionalUser
				.<UserDetails>map(user -> user)
				.orElseThrow(() ->
						new UsernameNotFoundException("User not found with email: " + username)
				);
    }

	// ---------- Helper ----------
	private User getAuthenticatedUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
			throw new IllegalStateException("No authenticated user found in security context");
		}

		return user;
	}

}
