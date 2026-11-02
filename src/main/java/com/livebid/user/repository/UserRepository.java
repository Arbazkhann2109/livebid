package com.livebid.user.repository;

import com.livebid.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<User> findByDisplayNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String displayName,
            String email,
            Pageable pageable
    );

    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            java.time.LocalDateTime start,
            java.time.LocalDateTime end
    );
}