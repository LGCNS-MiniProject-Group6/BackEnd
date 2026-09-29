package com.miniproject1.miniproject1.review;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miniproject1.miniproject1.commons.exception.BusinessException;
import com.miniproject1.miniproject1.commons.exception.ErrorCode;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

/** 현재 DB에서 검수용 사업정보를 조회하고 검수 결과를 저장합니다. */
@Repository
public class AiReviewRepository {

    private static final String FIND_BUSINESS_SQL = """
            SELECT
                business_id,
                region,
                industry,
                opening_date,
                business_type,
                employee_count,
                annual_revenue
            FROM business_info
            WHERE email = ?
            ORDER BY CASE WHEN is_default = TRUE THEN 0 ELSE 1 END, business_id
            LIMIT 1
            """;

    private static final String FIND_CATEGORIES_SQL = """
            SELECT category
            FROM business_interest_categories
            WHERE business_id = ?
            ORDER BY category
            """;

    private static final String SAVE_REVIEW_SQL = """
            INSERT INTO reviews (
                email,
                pblanc_id,
                business_snapshot,
                status,
                result_detail,
                is_outdated
            ) VALUES (?, ?, ?, ?, ?, FALSE)
            """;

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public AiReviewRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    /** 대표 사업정보가 있으면 우선 선택하고, 없으면 가장 먼저 등록한 정보를 사용합니다. */
    public Optional<AiBusinessProfile> findBusinessProfile(String email) {
        List<BusinessRow> businesses = jdbcTemplate.query(
                FIND_BUSINESS_SQL,
                (resultSet, rowNumber) -> new BusinessRow(
                        resultSet.getLong("business_id"),
                        resultSet.getString("region"),
                        resultSet.getString("industry"),
                        toLocalDate(resultSet.getDate("opening_date")),
                        resultSet.getString("business_type"),
                        resultSet.getInt("employee_count"),
                        resultSet.getLong("annual_revenue")),
                email);

        return businesses.stream().findFirst().map(business -> new AiBusinessProfile(
                business.region(),
                business.industry(),
                business.openingDate(),
                business.businessType(),
                business.employeeCount(),
                business.annualRevenue(),
                findInterestCategories(business.businessId())));
    }

    /** 검수 당시 사업정보와 검증된 모델 결과를 하나의 이력으로 저장합니다. */
    public long save(
            String email,
            String pblancId,
            AiBusinessProfile businessProfile,
            AiReviewStatus status,
            AiReviewAnalysis detail
    ) {
        String businessSnapshot = toJson(businessProfile);
        String resultDetail = toJson(detail);
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    SAVE_REVIEW_SQL,
                    Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, email);
            statement.setString(2, pblancId);
            statement.setString(3, businessSnapshot);
            statement.setString(4, status.name());
            statement.setString(5, resultDetail);
            return statement;
        }, keyHolder);

        Number reviewId = keyHolder.getKey();
        if (reviewId == null) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
        return reviewId.longValue();
    }

    private List<String> findInterestCategories(long businessId) {
        return jdbcTemplate.query(
                FIND_CATEGORIES_SQL,
                (resultSet, rowNumber) -> resultSet.getString("category"),
                businessId);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private static LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }

    private record BusinessRow(
            long businessId,
            String region,
            String industry,
            LocalDate openingDate,
            String businessType,
            int employeeCount,
            long annualRevenue
    ) {
    }
}
