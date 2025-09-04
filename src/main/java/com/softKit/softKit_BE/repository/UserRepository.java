package com.softKit.softKit_BE.repository;

import com.softKit.softKit_BE.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    Page<User> findAllByCreatedAt(Pageable pageable);

    boolean existsByEmail(String email);
}
