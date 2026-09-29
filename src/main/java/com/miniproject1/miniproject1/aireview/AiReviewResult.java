package com.miniproject1.miniproject1.aireview;

import java.time.LocalDateTime;

/** 팀원이 AiReviewService를 호출했을 때 받는 최종 검수 결과입니다. */
public record AiReviewResult(
        long reviewId,
        String pblancId,
        AiReviewStatus status,
        AiReviewAnalysis detail,
        LocalDateTime createdAt
) {
}
