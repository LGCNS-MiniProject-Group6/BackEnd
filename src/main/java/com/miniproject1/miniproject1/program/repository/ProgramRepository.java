package com.miniproject1.miniproject1.program.repository;

import com.miniproject1.miniproject1.program.entity.Program;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

public interface ProgramRepository extends JpaRepository<Program, String> {

        // [기존 코드] pblanc_id 기준 존재 여부 및 조회
        Optional<Program> findByPblancId(String pblancId);

        /**
         * 1. 상단 연관 검색어 (Top 5)
         * - 제목(title) 또는 소관기관(organization)에 키워드가 포함된 공고 중 최신순 Top 5
         */
        @Query("SELECT p FROM Program p WHERE " +
                        "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(p.organization) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                        "ORDER BY p.applyStartDate DESC")
        List<Program> findAutoCompleteTop5(@Param("keyword") String keyword, Pageable pageable);

        /**
         * 2. 하단 공고 통합 검색 (5개 필드 대상)
         * - title, category, organization, targetDescription, description 중 하나라도 포함 시
         * 조회
         */
        @Query("SELECT p FROM Program p WHERE " +
                        "(:category IS NULL OR :category = '' OR p.category = :category) AND (" +
                        ":keyword IS NULL OR :keyword = '' OR " +
                        "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(p.category) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(p.organization) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(p.targetDescription) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                        "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
        Page<Program> searchIntegrated(
                        @Param("keyword") String keyword,
                        @Param("category") String category,
                        Pageable pageable);

    /** 추천 후보: 대표 카테고리와 선택 필터를 적용한 신청 가능 공고입니다. */
    @Query("SELECT p FROM Program p WHERE " +
            "p.category IN :categories " +
            "AND (p.applyEndDate IS NULL OR p.applyEndDate >= :today) " +
            "AND (:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.targetDescription) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:region IS NULL OR :region = '' OR " +
            "LOWER(p.targetDescription) LIKE LOWER(CONCAT('%', :region, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :region, '%'))) " +
            "AND (:businessType IS NULL OR :businessType = '' OR " +
            "LOWER(p.targetDescription) LIKE LOWER(CONCAT('%', :businessType, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :businessType, '%')))")
    List<Program> findRecommendationCandidates(
            @Param("categories") List<String> categories,
            @Param("today") LocalDate today,
            @Param("keyword") String keyword,
            @Param("region") String region,
            @Param("businessType") String businessType);
}
