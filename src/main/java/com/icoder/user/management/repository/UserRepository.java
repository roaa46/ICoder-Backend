package com.icoder.user.management.repository;

import com.icoder.user.management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByHandle(String handle);

    Optional<User> findByEmail(String email);

    boolean existsByHandle(String handle);

    boolean existsByEmail(String email);
}
