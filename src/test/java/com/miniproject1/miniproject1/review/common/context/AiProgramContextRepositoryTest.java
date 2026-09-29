package com.miniproject1.miniproject1.review.common.context;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class AiProgramContextRepositoryTest {

    private JdbcTemplate jdbcTemplate;
    private AiProgramContextRepository repository;

    @BeforeEach
    void setUp() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                "jdbc:h2:mem:ai-common;MODE=MariaDB;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
                "sa",
                "");
        jdbcTemplate = new JdbcTemplate(dataSource);
        resetSchema();
        repository = new AiProgramContextRepository(jdbcTemplate);
    }

    @Test
    void 현재_DB_구조에서_공고와_AI_요약을_함께_조회한다() {
        insertProgramContext();

        AiProgramContext context = repository.findByPblancId("PBLN-1").orElseThrow();

        assertThat(context.title()).isEqualTo("온라인 판로 지원");
        assertThat(context.applyStartDate()).isEqualTo(LocalDate.of(2026, 9, 1));
        assertThat(context.summary().targetDescription()).isEqualTo("소상공인");
    }

    @Test
    void 파싱된_공고문_원문을_필요할_때만_조회한다() {
        insertProgramContext();

        assertThat(repository.findOriginalTextByPblancId("PBLN-1"))
                .contains("공고문 원문");
    }

    private void resetSchema() {
        jdbcTemplate.execute("DROP ALL OBJECTS");
        jdbcTemplate.execute("""
                CREATE TABLE programs (
                    pblanc_id VARCHAR(100) PRIMARY KEY,
                    title VARCHAR(255) NOT NULL,
                    category VARCHAR(50),
                    organization VARCHAR(100),
                    target_description TEXT,
                    description TEXT,
                    raw_apply_period VARCHAR(250),
                    apply_start_date DATE,
                    apply_end_date DATE
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE ai_summary (
                    pblanc_id VARCHAR(255) PRIMARY KEY,
                    biz_summary TEXT,
                    target_description TEXT,
                    support_content TEXT,
                    apply_method TEXT,
                    required_documents TEXT,
                    contact_info TEXT
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE program_documents (
                    pblanc_id VARCHAR(100) PRIMARY KEY,
                    original_text LONGTEXT
                )
                """);
    }

    private void insertProgramContext() {
        jdbcTemplate.update("""
                INSERT INTO programs (
                    pblanc_id, title, category, organization,
                    target_description, description, raw_apply_period,
                    apply_start_date, apply_end_date
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                "PBLN-1",
                "온라인 판로 지원",
                "판로",
                "중소벤처기업부",
                "소상공인",
                "온라인 판매 지원",
                "2026-09-01~2026-09-30",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 30));
        jdbcTemplate.update("""
                INSERT INTO ai_summary (
                    pblanc_id, biz_summary, target_description,
                    support_content, apply_method, required_documents, contact_info
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                "PBLN-1",
                "온라인 판로 확대 사업",
                "소상공인",
                "온라인 입점 지원",
                "온라인 신청",
                "사업자등록증",
                "중소벤처기업부");
        jdbcTemplate.update(
                "INSERT INTO program_documents (pblanc_id, original_text) VALUES (?, ?)",
                "PBLN-1",
                "공고문 원문");
    }
}
