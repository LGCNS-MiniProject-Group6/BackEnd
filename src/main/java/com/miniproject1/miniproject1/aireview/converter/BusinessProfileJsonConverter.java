package com.miniproject1.miniproject1.aireview.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.miniproject1.miniproject1.aireview.AiBusinessProfile;
import com.miniproject1.miniproject1.commons.exception.BusinessException;
import com.miniproject1.miniproject1.commons.exception.ErrorCode;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** 검수 시점의 사업정보 스냅샷을 reviews.business_snapshot JSON 컬럼으로 변환합니다. */
@Component
@Converter
public class BusinessProfileJsonConverter implements AttributeConverter<AiBusinessProfile, String> {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public String convertToDatabaseColumn(AiBusinessProfile attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public AiBusinessProfile convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            return objectMapper.readValue(dbData, AiBusinessProfile.class);
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
