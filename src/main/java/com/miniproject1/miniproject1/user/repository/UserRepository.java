package com.miniproject1.miniproject1.user.repository;

import com.miniproject1.miniproject1.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByEmail(String email);

    List<User> findAllByPhoneNumber(String phoneNumber);

    Optional<User> findByEmailAndPhoneNumber(String email, String phoneNumber);
}
