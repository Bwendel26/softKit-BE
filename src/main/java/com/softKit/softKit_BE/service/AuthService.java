package com.softKit.softKit_BE.service;

import com.softKit.softKit_BE.exception.EmailAlreadyInUseException;
import com.softKit.softKit_BE.exception.InvalidTokenException;
import com.softKit.softKit_BE.model.enums.Role;
import com.softKit.softKit_BE.model.enums.Status;
import com.softKit.softKit_BE.model.PasswordResetToken;
import com.softKit.softKit_BE.model.User;
import com.softKit.softKit_BE.model.dto.request.ForgotPasswordRequest;
import com.softKit.softKit_BE.model.dto.request.LoginRequest;
import com.softKit.softKit_BE.model.dto.request.RegisterRequest;
import com.softKit.softKit_BE.model.dto.request.ResetPasswordRequest;
import com.softKit.softKit_BE.model.dto.response.LoginResponse;
import com.softKit.softKit_BE.model.dto.response.RegisterResponse;
import com.softKit.softKit_BE.model.mapper.UserMapper;
import com.softKit.softKit_BE.repository.PasswordResetTokenRepository;
import com.softKit.softKit_BE.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository repository;
	private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(
			UserRepository repository,
			PasswordResetTokenRepository passwordResetTokenRepository,
			UserMapper mapper,
			PasswordEncoder passwordEncoder,
			JwtService jwtService,
			AuthenticationManager authenticationManager
    ) {
        this.repository = repository;
		this.passwordResetTokenRepository = passwordResetTokenRepository;
		this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()
                    )
            );

            User user = (User) auth.getPrincipal();
            String token = jwtService.generateToken(user);

            return new LoginResponse(
                    token,
                    "Bearer",
					jwtService.getExpirationMs(),
					mapper.toUserResponse(user)
            );
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Invalid username or password", e);
        }

    }

    @Transactional
    public RegisterResponse register(RegisterRequest registerRequest) {

		if (repository.existsByEmail(registerRequest.email())) {
			throw new EmailAlreadyInUseException(registerRequest.email());
		}

        User user = mapper.fromRegisterRequestToEntity(registerRequest);

        String hashedPassword = passwordEncoder.encode(registerRequest.password());

        user.setPasswordHash(hashedPassword);
        user.setRole(Role.CUSTOMER); //default profile
		user.setStatus(Status.ACTIVE); // ou PENDING TODO: verify flow for default user status
		user.setEmailVerifiedAt(OffsetDateTime.now());

        User savedUser = repository.save(user);

        return mapper.toRegisterResponse(savedUser);
    }

	@Transactional(readOnly = true)
	public LoginResponse refreshToken(String authHeader) {

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			throw new BadCredentialsException("Invalid Authorization header");
		}

		String token = authHeader.substring(7);

		try {
			String email = jwtService.extractUsername(token);

			User user = repository.findByEmail(email);
			if (user == null) {
				throw new BadCredentialsException("User not found for provided token");
			}

			if (!jwtService.isTokenValid(token, user)) {
				throw new BadCredentialsException("Invalid or expired token");
			}

			// Gera um NOVO token
			String newToken = jwtService.generateToken(user);

			return new LoginResponse(
					newToken,
					"Bearer",
					jwtService.getExpirationMs(),
					mapper.toUserResponse(user)
			);
		} catch (AuthenticationException e) {
			throw e;
		} catch (Exception e) {
			throw new BadCredentialsException("Invalid or expired token", e);
		}
	}

	@Transactional
	public void forgotPassword(ForgotPasswordRequest request) {

		User user = repository.findByEmail(request.email());

		// Segurança: não expor se o e-mail existe ou não
		if (user == null) {
			// silenciosamente não faz nada
			return;
		}

		String rawToken = UUID.randomUUID().toString();

		PasswordResetToken resetToken = new PasswordResetToken();
		resetToken.setToken(rawToken);
		resetToken.setUser(user);
		resetToken.setExpiresAt(OffsetDateTime.now().plusHours(1));

		passwordResetTokenRepository.save(resetToken);

		// TODO: implement send an email to user.getEmail() with activation link. Depends on register flow.
		// https://frontend/api/v1/auth/reset-password?token=" + rawToken
	}

	@Transactional
	public void resetPassword(ResetPasswordRequest request) {

		PasswordResetToken token = passwordResetTokenRepository.findByToken(request.token())
				.orElseThrow(() -> new InvalidTokenException("Invalid or expired password reset token"));

		if (token.isExpired() || token.isUsed()) {
			throw new InvalidTokenException("Invalid or expired password reset token");
		}

		if (!request.newPassword().equals(request.confirmNewPassword())) {
			throw new IllegalArgumentException("New password and confirmation do not match");
		}

		User user = token.getUser();

		user.changePassword(passwordEncoder.encode(request.newPassword()));
		token.setUsedAt(OffsetDateTime.now());

		repository.save(user);
		passwordResetTokenRepository.save(token);
	}

    @Transactional(readOnly = true)
    public boolean validateToken(String token) {
        try {
            String email = jwtService.extractUsername(token);
            User user = repository.findByEmail(email);

            if (user == null) {
                return false;
            }

			return jwtService.isTokenValid(token, user);

        } catch (Exception e) {
            return false;
        }
    }

    public String extractEmailFromToken(String token) {
        return jwtService.extractUsername(token);
    }


}
