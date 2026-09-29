package com.miniproject1.miniproject1.review.service;

import com.miniproject1.miniproject1.business.entity.BusinessInfo;
import com.miniproject1.miniproject1.business.repository.BusinessInfoRepository;
import com.miniproject1.miniproject1.commons.exception.BusinessException;
import com.miniproject1.miniproject1.commons.exception.ErrorCode;
import com.miniproject1.miniproject1.program.entity.Program;
import com.miniproject1.miniproject1.program.repository.ProgramRepository;
import com.miniproject1.miniproject1.review.dto.ReviewDetailResponse;
import com.miniproject1.miniproject1.review.dto.ReviewSummaryResponse;
import com.miniproject1.miniproject1.review.entity.Review;
import com.miniproject1.miniproject1.review.entity.ReviewCondition;
import com.miniproject1.miniproject1.review.entity.ReviewStatus;
import com.miniproject1.miniproject1.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BusinessInfoRepository businessInfoRepository;
    private final ProgramRepository programRepository;
    private final ReviewAnalyzer reviewAnalyzer;

    @Transactional
    public ReviewDetailResponse create(String email, String pblancId) {
        BusinessInfo businessInfo = businessInfoRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.BUSINESS_INFO_NOT_FOUND));
        Program program = programRepository.findById(pblancId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROGRAM_NOT_FOUND));

        ReviewAnalyzer.ReviewAnalysis analysis = reviewAnalyzer.analyze(program, businessInfo);
        Review review = Review.builder()
                .email(email)
                .pblancId(pblancId)
                .overallStatus(ReviewStatus.valueOf(analysis.overallStatus()))
                .model(analysis.model())
                .promptVersion(analysis.promptVersion())
                .documentHash(analysis.documentHash())
                .build();

        analysis.conditions().forEach(condition -> review.addCondition(ReviewCondition.builder()
                .conditionType(condition.conditionType())
                .status(condition.status())
                .reason(condition.reason())
                .page(condition.page())
                .evidenceText(condition.evidenceText())
                .build()));

        return ReviewDetailResponse.from(reviewRepository.save(review));
    }

    @Transactional(readOnly = true)
    public Page<ReviewSummaryResponse> findAll(String email, Pageable pageable) {
        return reviewRepository.findAllByEmailOrderByCreatedAtDesc(email, pageable)
                .map(ReviewSummaryResponse::from);
    }

    @Transactional(readOnly = true)
    public ReviewDetailResponse findById(String email, Long reviewId) {
        Review review = reviewRepository.findByIdAndEmail(reviewId, email)
                .orElseThrow(() -> new BusinessException(ErrorCode.REVIEW_NOT_FOUND));
        return ReviewDetailResponse.from(review);
    }
}
