package com.softKit.softKit_BE.service;

import com.softKit.softKit_BE.model.Enums.Profile;
import com.softKit.softKit_BE.model.User;
import com.softKit.softKit_BE.model.dto.LoginRequestDTO;
import com.softKit.softKit_BE.model.dto.LoginResponseDTO;
import com.softKit.softKit_BE.model.dto.UserCreateDTO;
import com.softKit.softKit_BE.model.mapper.UserMapper;
import com.softKit.softKit_BE.model.vo.UserResponseVO;
import com.softKit.softKit_BE.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository repository;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthService(
        UserRepository repository,
        UserMapper mapper,
        PasswordEncoder passwordEncoder,
        JwtService jwtService,
        AuthenticationManager authenticationManager
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO request) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()
                    )
            );

            User user = (User) auth.getPrincipal();
            String token = jwtService.generateToken(user);

            return new LoginResponseDTO(
                    token,
                    "Bearer",
                    3600000L // 1hour
            );
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Invalid username and password", e);
        }

    }

    @Transactional
    public UserResponseVO register(UserCreateDTO dto) {

        if (repository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = mapper.toEntity(dto);
        String hashedPassword = passwordEncoder.encode(dto.password());
        user.setPassword(hashedPassword);
        user.setProfile(Profile.CUSTOMER); //default profile
        User savedUser = repository.save(user);

        return mapper.toUserResponseVO(savedUser);
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
