package com.miniproject1.miniproject1.review;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miniproject1.miniproject1.review.AiReviewAnalysis.Condition;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class AiReviewRepositoryTest {

    private JdbcTemplate jdbcTemplate;
    private ObjectMapper objectMapper;
    private AiReviewRepository repository;

    @BeforeEach
    void setUp() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                "jdbc:h2:mem:ai-review;MODE=MariaDB;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
                "sa",
                "");
        jdbcTemplate = new JdbcTemplate(dataSource);
        objectMapper = new ObjectMapper().findAndRegisterModules();
        resetSchema();
        repository = new AiReviewRepository(jdbcTemplate, objectMapper);
    }

    @Test
    void 대표_사업정보와_관심분야를_조회한다() {
        insertBusinessInformation();

        AiBusinessProfile profile = repository.findBusinessProfile("user@example.com").orElseThrow();

        assertThat(profile.region()).isEqualTo("서울");
        assertThat(profile.industry()).isEqualTo("카페");
        assertThat(profile.interestCategories()).containsExactly("마케팅", "판로");
    }

    @Test
    void 검수_스냅샷과_결과를_저장한다() throws Exception {
        AiBusinessProfile profile = new AiBusinessProfile(
                "서울",
                "카페",
                LocalDate.of(2024, 3, 15),
                "개인사업자",
                0,
                80_000_000L,
                List.of("마케팅"));
        AiReviewAnalysis analysis = new AiReviewAnalysis(
                List.of(new Condition(
                        "지역",
                        AiReviewStatus.MATCHED,
                        "서울",
                        "서울 소재 사업자",
                        "지역 요건을 충족합니다.",
                        "서울 소재 소상공인을 대상으로 합니다.")),
                List.of(),
                List.of());

        long reviewId = repository.save(
                "user@example.com",
                "PBLN-1",
                profile,
                AiReviewStatus.MATCHED,
                analysis);

        assertThat(reviewId).isPositive();
        JsonNode resultDetail = objectMapper.readTree(jdbcTemplate.queryForObject(
                "SELECT result_detail FROM reviews WHERE review_id = ?",
                String.class,
                reviewId));
        assertThat(resultDetail.at("/conditions/0/status").asText()).isEqualTo("MATCHED");
    }

    private void resetSchema() {
        jdbcTemplate.execute("DROP ALL OBJECTS");
        jdbcTemplate.execute("""
                CREATE TABLE business_info (
                    business_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    email VARCHAR(100) NOT NULL,
                    company_name VARCHAR(100),
                    industry VARCHAR(50) NOT NULL,
                    region VARCHAR(50) NOT NULL,
                    opening_date DATE NOT NULL,
                    business_type VARCHAR(20) NOT NULL,
                    employee_count INT NOT NULL,
                    annual_revenue BIGINT NOT NULL,
                    is_default BOOLEAN
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE business_interest_categories (
                    interest_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    business_id BIGINT NOT NULL,
                    category VARCHAR(50) NOT NULL
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE reviews (
                    review_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    email VARCHAR(100) NOT NULL,
                    pblanc_id VARCHAR(100) NOT NULL,
                    business_snapshot LONGTEXT NOT NULL,
                    status VARCHAR(20) NOT NULL,
                    result_detail LONGTEXT NOT NULL,
                    is_outdated BOOLEAN DEFAULT FALSE
                )
                """);
    }

    private void insertBusinessInformation() {
        jdbcTemplate.update("""
                INSERT INTO business_info (
                    email, industry, region, opening_date, business_type,
                    employee_count, annual_revenue, is_default
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                "user@example.com",
                "제조업",
                "부산",
                LocalDate.of(2020, 1, 1),
                "법인사업자",
                10,
                1_000_000_000L,
                false);
        jdbcTemplate.update("""
                INSERT INTO business_info (
                    email, industry, region, opening_date, business_type,
                    employee_count, annual_revenue, is_default
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                "user@example.com",
                "카페",
                "서울",
                LocalDate.of(2024, 3, 15),
                "개인사업자",
                0,
                80_000_000L,
                true);

        Long businessId = jdbcTemplate.queryForObject(
                "SELECT business_id FROM business_info WHERE is_default = TRUE",
                Long.class);
        jdbcTemplate.update(
                "INSERT INTO business_interest_categories (business_id, category) VALUES (?, ?)",
                businessId,
                "판로");
        jdbcTemplate.update(
                "INSERT INTO business_interest_categories (business_id, category) VALUES (?, ?)",
                businessId,
                "마케팅");
    }
}
