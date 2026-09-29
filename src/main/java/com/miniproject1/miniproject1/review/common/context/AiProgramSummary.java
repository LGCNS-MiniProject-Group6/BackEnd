package com.miniproject1.miniproject1.review.common.context;

/**
 * 현재 ai_summary 테이블에 저장된 공고 요약 6개 항목입니다.
 */
public record AiProgramSummary(
        String bizSummary,
        String targetDescription,
        String supportContent,
        String applyMethod,
        String requiredDocuments,
        String contactInfo
) {
}
