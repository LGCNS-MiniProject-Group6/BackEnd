package com.miniproject1.miniproject1.review.controller;

import com.miniproject1.miniproject1.review.dto.ReviewDetailResponse;
import com.miniproject1.miniproject1.review.dto.ReviewSummaryResponse;
import com.miniproject1.miniproject1.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Review", description = "AI 신청 전 적합성 검수 API")
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "AI 신청 전 검수", description = "토큰의 이메일로 사업정보를 조회해 공고와 비교하고 검수 이력을 저장합니다.")
    @PostMapping("/{pblancId}")
    public ResponseEntity<ReviewDetailResponse> create(
            Authentication authentication,
            @Parameter(description = "지원사업 공고 ID") @PathVariable String pblancId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.create(authentication.getName(), pblancId));
    }

    @Operation(summary = "내 검수 이력 전체 조회")
    @GetMapping
    public ResponseEntity<Page<ReviewSummaryResponse>> findAll(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        PageRequest pageable = PageRequest.of(safePage, safeSize,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(reviewService.findAll(authentication.getName(), pageable));
    }

    @Operation(summary = "검수 결과 상세 조회")
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewDetailResponse> findById(
            Authentication authentication,
            @PathVariable Long reviewId) {
        return ResponseEntity.ok(reviewService.findById(authentication.getName(), reviewId));
    }
}
