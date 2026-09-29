package com.miniproject1.miniproject1.aireview;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiReviewRepository extends JpaRepository<AiReview, Long> {

    Page<AiReview> findAllByEmailOrderByCreatedAtDesc(String email, Pageable pageable);

    Optional<AiReview> findByIdAndEmail(Long id, String email);
}
