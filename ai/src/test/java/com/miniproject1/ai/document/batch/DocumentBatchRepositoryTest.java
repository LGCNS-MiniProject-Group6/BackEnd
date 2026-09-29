package com.miniproject1.ai.document.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

@ExtendWith(MockitoExtension.class)
class DocumentBatchRepositoryTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private DocumentBatchRepository documentBatchRepository;

    @Test
    void findsProgramsMissingSummaryOrOriginalText() {
        documentBatchRepository.findPendingPblancIds(1500);

        // 이미 요약된 공고도 원문이 비어 있으면 복구 대상에 포함되는지 확인합니다.
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).query(
                sqlCaptor.capture(),
                argThat((RowMapper<String> rowMapper) -> rowMapper != null),
                eq(1500));

        assertThat(sqlCaptor.getValue())
                .contains("LEFT JOIN ai_summary")
                .contains("LEFT JOIN program_documents")
                .contains("a.pblanc_id IS NULL")
                .contains("d.original_text IS NULL")
                .contains("TRIM(d.original_text) = ''");
    }
}
