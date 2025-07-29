package com.softKit.softKit_BE.service;

import com.softKit.softKit_BE.model.User;
import com.softKit.softKit_BE.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    UserRepository repository;

    public Optional<User> getUserById(Long id) {
        return repository.findById(id);
    }

    public User save(User user) {
        return repository.save(user);
    }

    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }
}
