package com.miniproject1.miniproject1.favorites.repository;

import com.miniproject1.miniproject1.favorites.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    boolean existsByEmailAndProgramId(String email, String programId);

    List<Favorite> findAllByEmailOrderByCreatedAtDesc(String email);

    long deleteByEmailAndProgramId(String email, String programId);
}
