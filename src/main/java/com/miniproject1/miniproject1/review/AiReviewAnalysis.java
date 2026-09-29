package com.miniproject1.miniproject1.review;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** 모델이 생성하고 서버가 검증한 검수 세부 결과입니다. */
public record AiReviewAnalysis(
        @JsonProperty(required = true) List<Condition> conditions,
        @JsonProperty(required = true) List<GroundedText> warnings,
        @JsonProperty(required = true) List<GroundedText> documents
) {

    /** 공고의 자격 요건과 사용자 사업정보를 비교한 결과입니다. */
    public record Condition(
            @JsonProperty(required = true) String condition,
            @JsonProperty(required = true) AiReviewStatus status,
            @JsonProperty(required = true) String userValue,
            @JsonProperty(required = true) String requirement,
            String reason,
            @JsonProperty(required = true) String evidence
    ) {
        /** 모델이 설명을 생략한 경우 판정 상태에 맞는 기본 설명만 보완합니다. */
        public Condition {
            if (status != null && (reason == null || reason.isBlank())) {
                reason = switch (status) {
                    case MATCHED -> "사업정보가 공고 요건을 충족합니다.";
                    case UNMATCHED -> "사업정보가 공고 요건을 충족하지 않습니다.";
                    case NEED_CHECK -> "제공된 정보만으로 충족 여부를 판단할 수 없습니다.";
                };
            }
        }
    }

    /** 원문 근거를 포함하는 주의사항 또는 제출서류입니다. */
    public record GroundedText(
            @JsonProperty(required = true) String text,
            @JsonProperty(required = true) String evidence
    ) {
    }
}
