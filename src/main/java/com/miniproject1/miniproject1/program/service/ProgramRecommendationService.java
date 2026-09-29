package com.miniproject1.miniproject1.program.service;

import com.miniproject1.miniproject1.aireview.AiReview;
import com.miniproject1.miniproject1.aireview.AiReviewRepository;
import com.miniproject1.miniproject1.aireview.AiReviewStatus;
import com.miniproject1.miniproject1.commons.exception.BusinessException;
import com.miniproject1.miniproject1.commons.exception.ErrorCode;
import com.miniproject1.miniproject1.program.dto.response.ProgramRecommendationResponseDTO;
import com.miniproject1.miniproject1.program.entity.Program;
import com.miniproject1.miniproject1.program.repository.ProgramRepository;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProgramRecommendationService {

    private static final int MAX_RECOMMENDATIONS = 10;

    private static final Map<String, List<String>> CATEGORY_GROUPS = Map.of(
            "경영", List.of("경영", "컨설팅", "고용환경개선"),
            "기술", List.of("기술", "기술사업화/이전/지도", "시험/인증"),
            "금융", List.of("금융", "펀드/투자"),
            "수출", List.of("수출", "해외진출", "해외진출준비", "시장개척"),
            "인력", List.of("인력", "고용유지"),
            "내수", List.of("내수", "온라인", "오프라인"),
            "창업", List.of("창업", "사업화지원", "디자인/상품화/사업화", "시설/입지지원"),
            "기타", List.of("기타", "혼합(단독+공동)"));

    /** AI가 각 조건에 반환한 상태를 추천 점수로 변환하는 기준표입니다. */
    private static final int MATCHED_SCORE = 100;
    private static final int NEED_CHECK_SCORE = 50;
    private static final int UNMATCHED_SCORE = 0;
    private static final int DEFAULT_CONDITION_WEIGHT = 10;
    private static final Map<String, Integer> CONDITION_WEIGHTS = Map.of(
            "지역", 25,
            "사업자", 20,
            "업종", 20,
            "개업", 15,
            "창업", 15,
            "근로자", 10,
            "직원", 10,
            "매출", 10,
            "관심", 10);

    private final ProgramRepository programRepository;
    private final AiReviewRepository aiReviewRepository;

    public Page<ProgramRecommendationResponseDTO> recommend(
            String email,
            String category,
            String region,
            String businessType,
            String keyword,
            Pageable pageable) {
        String normalizedCategory = normalize(category);
        List<String> categories = CATEGORY_GROUPS.get(normalizedCategory);
        if (categories == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "대표 카테고리는 경영, 기술, 금융, 수출, 인력, 내수, 창업, 기타 중 하나여야 합니다.");
        }

        List<Program> candidates = programRepository.findRecommendationCandidates(
                categories,
                LocalDate.now(),
                normalizeNullable(keyword),
                normalizeNullable(region),
                normalizeNullable(businessType));

        Map<String, AiReview> latestReviewByProgram = latestReviews(email);
        List<ProgramRecommendationResponseDTO> sorted = candidates.stream()
                .sorted(recommendationComparator(latestReviewByProgram))
                .map(program -> {
                    AiReview review = latestReviewByProgram.get(program.getPblancId());
                    return ProgramRecommendationResponseDTO.from(
                            program,
                            review == null ? null : review.getStatus(),
                            review == null ? null : calculateScore(review));
                })
                .toList();

        int pageSize = Math.min(pageable.getPageSize(), MAX_RECOMMENDATIONS);
        int topEnd = Math.min(MAX_RECOMMENDATIONS, sorted.size());
        int start = Math.min((int) pageable.getOffset(), topEnd);
        int end = Math.min(start + pageSize, topEnd);
        return new PageImpl<>(sorted.subList(start, end), pageable, topEnd);
    }

    private Map<String, AiReview> latestReviews(String email) {
        Map<String, AiReview> reviews = new HashMap<>();
        for (AiReview review : aiReviewRepository.findAllByEmailOrderByCreatedAtDesc(email)) {
            reviews.putIfAbsent(review.getPblancId(), review);
        }
        return reviews;
    }

    private Comparator<Program> recommendationComparator(Map<String, AiReview> reviews) {
        return Comparator
                .comparingInt((Program program) -> scoreOrder(reviews.get(program.getPblancId())))
                .reversed()
                .thenComparing(Program::getApplyEndDate,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Program::getApiUpdatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder()));
    }

    private int scoreOrder(AiReview review) {
        return review == null ? -1 : calculateScore(review);
    }

    /** 조건별 AI 상태를 평균 내어 공고의 최종 추천 점수(0~100)를 계산합니다. */
    private int calculateScore(AiReview review) {
        if (review.getResultDetail() == null
                || review.getResultDetail().conditions() == null
                || review.getResultDetail().conditions().isEmpty()) {
            return scoreOf(review.getStatus());
        }
        int totalWeight = 0;
        int weightedScore = 0;
        for (var condition : review.getResultDetail().conditions()) {
            int weight = conditionWeight(condition.condition());
            weightedScore += scoreOf(condition.status()) * weight;
            totalWeight += weight;
        }
        double average = totalWeight == 0
                ? scoreOf(review.getStatus())
                : (double) weightedScore / totalWeight;
        return (int) Math.round(average);
    }

    private int conditionWeight(String condition) {
        if (condition == null) {
            return DEFAULT_CONDITION_WEIGHT;
        }
        return CONDITION_WEIGHTS.entrySet().stream()
                .filter(entry -> condition.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(DEFAULT_CONDITION_WEIGHT);
    }

    private int scoreOf(AiReviewStatus status) {
        if (status == null) {
            return 0;
        }
        return switch (status) {
            case MATCHED -> MATCHED_SCORE;
            case NEED_CHECK -> NEED_CHECK_SCORE;
            case UNMATCHED -> UNMATCHED_SCORE;
        };
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeNullable(String value) {
        String normalized = normalize(value);
        return normalized.isEmpty() ? null : normalized;
    }
}
