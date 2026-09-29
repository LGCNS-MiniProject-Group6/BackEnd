package com.miniproject1.ai.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
class ProgramDocumentRepositoryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private ProgramDocumentRepository programDocumentRepository;

    @Test
    void saveOriginalTextUpdatesExistingProgramDocument() {
        programDocumentRepository.saveOriginalText("PBLN_TEST", "파싱한 공고문 원문");

        // 기존 문서 행에서도 original_text가 실제로 갱신되는 SQL인지 확인합니다.
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).update(
                sqlCaptor.capture(),
                eq("PBLN_TEST"),
                eq("파싱한 공고문 원문"));

        assertThat(sqlCaptor.getValue())
                .contains("ON DUPLICATE KEY UPDATE original_text = VALUES(original_text)");
    }
}
