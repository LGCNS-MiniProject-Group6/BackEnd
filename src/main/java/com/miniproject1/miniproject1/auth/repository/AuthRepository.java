package com.miniproject1.miniproject1.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.miniproject1.miniproject1.users.domain.entity.User;

public interface AuthRepository extends JpaRepository<User, String> {

    // 인증/계정 검증 전용 쿼리
    boolean existsByEmail(String email);
}