package com.miniproject1.miniproject1.program.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.miniproject1.miniproject1.aireview.AiReview;
import com.miniproject1.miniproject1.aireview.AiReviewRepository;
import com.miniproject1.miniproject1.aireview.AiReviewStatus;
import com.miniproject1.miniproject1.aireview.common.client.AiModelClient;
import com.miniproject1.miniproject1.aireview.common.prompt.AiPrompt;
import com.miniproject1.miniproject1.business.entity.BusinessInfo;
import com.miniproject1.miniproject1.business.repository.BusinessInfoRepository;
import com.miniproject1.miniproject1.commons.exception.BusinessException;
import com.miniproject1.miniproject1.program.dto.response.ProgramRecommendationResponseDTO;
import com.miniproject1.miniproject1.program.entity.Program;
import com.miniproject1.miniproject1.program.repository.AiSummaryRepository;
import com.miniproject1.miniproject1.program.repository.ProgramRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class ProgramRecommendationServiceTest {

    @Mock
    private ProgramRepository programRepository;

    @Mock
    private AiReviewRepository aiReviewRepository;

    @Mock
    private AiSummaryRepository aiSummaryRepository;

    @Mock
    private BusinessInfoRepository businessInfoRepository;

    @Mock
    private AiModelClient modelClient;

    @InjectMocks
    private ProgramRecommendationService service;

    @Test
    void AI가_반환한_순서와_점수_근거를_그대로_반영한다() {
        Program top = program("P-TOP", "2026-10-20");
        Program second = program("P-SECOND", "2026-10-10");
        Program excluded = program("P-EXCLUDED", "2026-10-05");

        givenBusinessInfo();
        when(programRepository.findRecommendationCandidates(
                eq(List.of("경영", "컨설팅", "고용환경개선")), any(), eq(null), eq(null), eq(null)))
                .thenReturn(List.of(top, second, excluded));
        when(aiSummaryRepository.findAllById(any())).thenReturn(List.of());
        when(aiReviewRepository.findAllByEmailOrderByCreatedAtDesc("user@example.com")).thenReturn(List.of());
        when(modelClient.generateStructured(any(AiPrompt.class), eq(ProgramMatchRanking.class)))
                .thenReturn(new ProgramMatchRanking(List.of(
                        new ProgramMatchRanking.RankedProgram(1, 95, "지역과 업종이 정확히 일치합니다."),
                        new ProgramMatchRanking.RankedProgram(2, 60, "업종만 일치합니다."))));

        Page<ProgramRecommendationResponseDTO> result = service.recommend(
                "user@example.com", "경영", null, null, null, PageRequest.of(0, 10));

        List<ProgramRecommendationResponseDTO> content = result.getContent();
        assertEquals(2, content.size());
        assertEquals("P-TOP", content.get(0).getPblancId());
        assertEquals(95, content.get(0).getRecommendationScore());
        assertEquals("지역과 업종이 정확히 일치합니다.", content.get(0).getMatchReason());
        assertEquals("P-SECOND", content.get(1).getPblancId());
        assertEquals(60, content.get(1).getRecommendationScore());
    }

    @Test
    void 후보목록_범위를_벗어난_번호는_결과에서_제외된다() {
        Program only = program("P-ONLY", "2026-10-20");

        givenBusinessInfo();
        when(programRepository.findRecommendationCandidates(any(), any(), any(), any(), any()))
                .thenReturn(List.of(only));
        when(aiSummaryRepository.findAllById(any())).thenReturn(List.of());
        when(aiReviewRepository.findAllByEmailOrderByCreatedAtDesc("user@example.com")).thenReturn(List.of());
        when(modelClient.generateStructured(any(AiPrompt.class), eq(ProgramMatchRanking.class)))
                .thenReturn(new ProgramMatchRanking(List.of(
                        new ProgramMatchRanking.RankedProgram(2, 90, "후보 목록에 없는 번호"),
                        new ProgramMatchRanking.RankedProgram(1, 80, "실제 후보입니다."))));

        Page<ProgramRecommendationResponseDTO> result = service.recommend(
                "user@example.com", "경영", null, null, null, PageRequest.of(0, 10));

        assertEquals(1, result.getContent().size());
        assertEquals("P-ONLY", result.getContent().get(0).getPblancId());
    }

    @Test
    void 검수이력이_있으면_reviewStatus가_함께_반환된다() {
        Program program = program("P-REVIEWED", "2026-10-20");

        givenBusinessInfo();
        when(programRepository.findRecommendationCandidates(any(), any(), any(), any(), any()))
                .thenReturn(List.of(program));
        when(aiSummaryRepository.findAllById(any())).thenReturn(List.of());
        when(aiReviewRepository.findAllByEmailOrderByCreatedAtDesc("user@example.com"))
                .thenReturn(List.of(new AiReview("user@example.com", "P-REVIEWED", null, AiReviewStatus.MATCHED, null)));
        when(modelClient.generateStructured(any(AiPrompt.class), eq(ProgramMatchRanking.class)))
                .thenReturn(new ProgramMatchRanking(List.of(
                        new ProgramMatchRanking.RankedProgram(1, 88, "이미 검수한 공고와 조건이 유사합니다."))));

        Page<ProgramRecommendationResponseDTO> result = service.recommend(
                "user@example.com", "경영", null, null, null, PageRequest.of(0, 10));

        assertEquals(AiReviewStatus.MATCHED, result.getContent().get(0).getReviewStatus());
    }

    @Test
    void 사업정보가_없으면_예외를_던진다() {
        when(businessInfoRepository.findFirstByEmailOrderByIsDefaultDescBusinessIdAsc("user@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> service.recommend(
                "user@example.com", "경영", null, null, null, PageRequest.of(0, 10)));
    }

    @Test
    void 대표카테고리가_아니면_예외를_던진다() {
        assertThrows(BusinessException.class, () -> service.recommend(
                "user@example.com", "요리", null, null, null, PageRequest.of(0, 10)));
    }

    @Test
    void 후보가_없으면_빈_페이지를_반환한다() {
        givenBusinessInfo();
        when(programRepository.findRecommendationCandidates(any(), any(), any(), any(), any()))
                .thenReturn(List.of());

        Page<ProgramRecommendationResponseDTO> result = service.recommend(
                "user@example.com", "경영", null, null, null, PageRequest.of(0, 10));

        assertTrue(result.getContent().isEmpty());
    }

    private void givenBusinessInfo() {
        when(businessInfoRepository.findFirstByEmailOrderByIsDefaultDescBusinessIdAsc("user@example.com"))
                .thenReturn(Optional.of(BusinessInfo.builder()
                        .email("user@example.com")
                        .industry("소프트웨어 개발업")
                        .region("서울특별시 강남구")
                        .businessType("개인사업자")
                        .openingDate(LocalDate.of(2020, 1, 15))
                        .employeeCount(5)
                        .annualRevenue(300_000_000L)
                        .build()));
    }

    private Program program(String id, String endDate) {
        return Program.builder()
                .pblancId(id)
                .title(id)
                .applyEndDate(LocalDate.parse(endDate))
                .build();
    }
}
