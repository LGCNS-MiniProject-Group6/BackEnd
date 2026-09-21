package com.miniproject1.miniproject1.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.miniproject1.miniproject1.domain.user.entity.User;

public interface UserRepository extends JpaRepository<User, String> {

    boolean existsByEmail(String email);
}
