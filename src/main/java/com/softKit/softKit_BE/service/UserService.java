package com.softKit.softKit_BE.service;

import com.softKit.softKit_BE.model.User;
import com.softKit.softKit_BE.model.dto.UserCreateDTO;
import com.softKit.softKit_BE.model.mapper.ModelMapper;
import com.softKit.softKit_BE.model.vo.UserResponseVO;
import com.softKit.softKit_BE.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    UserRepository repository;

    private final ModelMapper mapper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository repository,
                       ModelMapper mapper,
                       PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponseVO getUserById(Long id) {
        User user = repository.findById(id).orElseThrow(() ->
                new RuntimeException("User nor found"));
        return mapper.toResponse(user);
    }

    public UserResponseVO createUser(UserCreateDTO dto) {
        if (repository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("Email already in use");
        }

        User user = mapper.toEntity(dto);

        String hashedPassword = passwordEncoder.encode(dto.password());
        user.setPassword(hashedPassword);

        User savedUser = repository.save(user);

        return mapper.toResponse(savedUser);
    }

    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }
}
