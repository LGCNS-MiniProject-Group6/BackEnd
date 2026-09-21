package com.miniproject1.miniproject1.domain.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.miniproject1.miniproject1.domain.auth.entity.User;

public interface UserRepository extends JpaRepository<User, String> {

    boolean existsByEmail(String email);
}
