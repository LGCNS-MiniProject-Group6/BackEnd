package com.miniproject1.miniproject1.user.repository;

import com.miniproject1.miniproject1.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
}
