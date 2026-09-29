package com.miniproject1.miniproject1.program.dto.response;

import com.miniproject1.miniproject1.program.entity.AiSummary;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AiSummaryResponseDTO {

    /*
     * 1. 이 데이터는 Ai_summary 테이블의 데이터를 기반으로 응답한다.
     * 2. DTO의 필드명은 프론트엔드에서 사용할 변수명과 일치시킨다.
     * 3. 이 테이블을 따로 만드는 이유는 Ai_summary 테이블과 programs 테이블에 컬럼명이 동일한 부분이 존재하기 때문이다.
     */
    private String bizSummary; // 사업개요
    private String targetDescription; // 지원대상
    private String supportContent; // 지원내용
    private String applyMethod; // 신청방법
    private String requiredDocuments; // 준비서류
    private String contactInfo; // 문의처
    private LocalDateTime updatedAt; // 요약 생성/수정 일시

    public static AiSummaryResponseDTO from(AiSummary entity) {
        if (entity == null) {
            return null;
        }
        return AiSummaryResponseDTO.builder()
                .bizSummary(entity.getBizSummary())
                .targetDescription(entity.getTargetDescription())
                .supportContent(entity.getSupportContent())
                .applyMethod(entity.getApplyMethod())
                .requiredDocuments(entity.getRequiredDocuments())
                .contactInfo(entity.getContactInfo())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}