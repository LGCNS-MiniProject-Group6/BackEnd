package com.miniproject1.miniproject1.review.common.context;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/** 현재 DB에서 AI 검수에 필요한 공고 문맥을 조회합니다. */
@Repository
public class AiProgramContextRepository {

    private static final String FIND_CONTEXT_SQL = """
            SELECT
                p.pblanc_id,
                p.title,
                p.category,
                p.organization,
                p.target_description,
                p.description,
                p.raw_apply_period,
                p.apply_start_date,
                p.apply_end_date,
                s.biz_summary,
                s.target_description AS summary_target_description,
                s.support_content,
                s.apply_method,
                s.required_documents,
                s.contact_info
            FROM programs p
            LEFT JOIN ai_summary s ON s.pblanc_id = p.pblanc_id
            WHERE p.pblanc_id = ?
            """;

    private static final String FIND_ORIGINAL_TEXT_SQL = """
            SELECT original_text
            FROM program_documents
            WHERE pblanc_id = ?
              AND original_text IS NOT NULL
              AND TRIM(original_text) <> ''
            """;

    private final JdbcTemplate jdbcTemplate;

    public AiProgramContextRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** 공고 기본 정보와 미리 생성된 AI 요약을 함께 조회합니다. */
    public Optional<AiProgramContext> findByPblancId(String pblancId) {
        requirePblancId(pblancId);

        List<AiProgramContext> contexts = jdbcTemplate.query(
                FIND_CONTEXT_SQL,
                (resultSet, rowNumber) -> new AiProgramContext(
                        resultSet.getString("pblanc_id"),
                        resultSet.getString("title"),
                        resultSet.getString("category"),
                        resultSet.getString("organization"),
                        resultSet.getString("target_description"),
                        resultSet.getString("description"),
                        resultSet.getString("raw_apply_period"),
                        toLocalDate(resultSet.getDate("apply_start_date")),
                        toLocalDate(resultSet.getDate("apply_end_date")),
                        new AiProgramSummary(
                                resultSet.getString("biz_summary"),
                                resultSet.getString("summary_target_description"),
                                resultSet.getString("support_content"),
                                resultSet.getString("apply_method"),
                                resultSet.getString("required_documents"),
                                resultSet.getString("contact_info"))),
                pblancId);

        return contexts.stream().findFirst();
    }

    /** 상세 근거가 필요할 때만 파싱된 공고문 원문을 별도로 조회합니다. */
    public Optional<String> findOriginalTextByPblancId(String pblancId) {
        requirePblancId(pblancId);

        List<String> originalTexts = jdbcTemplate.query(
                FIND_ORIGINAL_TEXT_SQL,
                (resultSet, rowNumber) -> resultSet.getString("original_text"),
                pblancId);

        return originalTexts.stream().findFirst();
    }

    private static LocalDate toLocalDate(Date date) {
        return date == null ? null : date.toLocalDate();
    }

    private static void requirePblancId(String pblancId) {
        if (pblancId == null || pblancId.isBlank()) {
            throw new IllegalArgumentException("pblancId must not be blank");
        }
    }
}
