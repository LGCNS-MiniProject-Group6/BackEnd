package com.miniproject1.miniproject1.aireview.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miniproject1.miniproject1.aireview.AiReviewAnalysis;
import com.miniproject1.miniproject1.commons.exception.BusinessException;
import com.miniproject1.miniproject1.commons.exception.ErrorCode;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** AI 검수 결과를 reviews.result_detail JSON 컬럼으로 변환합니다. */
@Component
@Converter
public class AiReviewAnalysisJsonConverter implements AttributeConverter<AiReviewAnalysis, String> {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public String convertToDatabaseColumn(AiReviewAnalysis attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(attribute);
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public AiReviewAnalysis convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            return objectMapper.readValue(dbData, AiReviewAnalysis.class);
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
