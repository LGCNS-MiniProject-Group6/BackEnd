package com.miniproject1.miniproject1.review.repository;

import com.miniproject1.miniproject1.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findAllByEmailOrderByCreatedAtDesc(String email, Pageable pageable);

    @EntityGraph(attributePaths = "conditions")
    java.util.Optional<Review> findByIdAndEmail(Long id, String email);
}
