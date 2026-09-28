package com.miniproject1.miniproject1.favorites.repository;

import com.miniproject1.miniproject1.favorites.entity.Favorite;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    boolean existsByEmailAndProgram_PblancId(String email, String pblancId);

    /** 공고 정보를 함께 응답하므로 program을 한 번에 조회합니다(N+1 방지). */
    @EntityGraph(attributePaths = "program")
    List<Favorite> findAllByEmailOrderByCreatedAtDesc(String email);

    long deleteByEmailAndProgram_PblancId(String email, String pblancId);
}
