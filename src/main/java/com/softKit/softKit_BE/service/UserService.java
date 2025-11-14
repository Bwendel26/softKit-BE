package com.softKit.softKit_BE.service;

import com.softKit.softKit_BE.model.DataPasswordReset;
import com.softKit.softKit_BE.model.User;
import com.softKit.softKit_BE.model.dto.UserCreateDTO;
import com.softKit.softKit_BE.model.dto.UserUpdateDTO;
import com.softKit.softKit_BE.model.mapper.ModelMapper;
import com.softKit.softKit_BE.model.mapper.UserMapper;
import com.softKit.softKit_BE.model.vo.UserResponseVO;
import com.softKit.softKit_BE.model.vo.UserVO;
import com.softKit.softKit_BE.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@PreAuthorize("hasRole('ADMIN')")
public class UserService implements UserDetailsService {

    UserRepository repository;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository repository,
                       UserMapper mapper,
                       PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponseVO getUserById(Long id) {
        User user = repository.findById(id).orElseThrow(() ->
                new RuntimeException("User nor found"));
        return mapper.toUserResponseVO(user);
    }

    public UserResponseVO createUser(UserCreateDTO dto) {
        if (repository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("Email already in use");
        }

        User user = mapper.toEntity(dto);
//        TODO: CREATE A SOLUTION TO WHEN THE USER RECEIVES AN ACCESS HE MUST HAVE TO CHANGE THE PASSWORD - COMMENTED CODE IS GENERATING A NEW RANDOM PASSWORD
//        String firstPassword = UUID.randomUUID().toString().substring(0, 8);
//        System.out.println("Generated PASSWORD: " + firstPassword);

        String hashedPassword = passwordEncoder.encode(dto.password());
        user.setPassword(hashedPassword);

        User savedUser = repository.save(user);

        return mapper.toUserResponseVO(savedUser);
    }

    public UserResponseVO updateUser(Long id, UserUpdateDTO dto) {
        User user = repository.findById(id).orElseThrow(() ->
                new RuntimeException("User nor found"));
        mapper.updateEntityFromDto(dto, user);
        User updatedUser = repository.save(user);

        return mapper.toUserResponseVO(repository.save(user));
    }

    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    public boolean existsById(Long id) {
        return repository.existsById(id);
    }

    public boolean existsByEmailAndIdNot(String email, Long id) {
        return repository.existsByEmailAndIdNot(email, id);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return repository.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found!"));
    }

    public void changePassword(DataPasswordReset data, User loggedIn) {
        if(!passwordEncoder.matches(data.password(), loggedIn.getPassword())
        || !data.newPassword().equals(data.confirmNewPassword())) {
            throw new IllegalArgumentException("Passwords don't match");
        }

        String encryptedPassword = passwordEncoder.encode(data.newPassword());
        loggedIn.changePassword(encryptedPassword);

        repository.save(loggedIn);
    }
}
