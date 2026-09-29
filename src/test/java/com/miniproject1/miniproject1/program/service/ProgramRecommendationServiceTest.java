package com.miniproject1.miniproject1.program.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.miniproject1.miniproject1.aireview.AiReview;
import com.miniproject1.miniproject1.aireview.AiReviewRepository;
import com.miniproject1.miniproject1.aireview.AiReviewStatus;
import com.miniproject1.miniproject1.aireview.AiReviewAnalysis;
import com.miniproject1.miniproject1.program.entity.Program;
import com.miniproject1.miniproject1.program.repository.ProgramRepository;
import java.util.List;
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

    @InjectMocks
    private ProgramRecommendationService service;

    @Test
    void AI_조건점수가_높은공고부터정렬되고_미검수는마지막이다() {
        Program unmatched = program("P-UNMATCHED", "2026-10-20");
        Program matched = program("P-MATCHED", "2026-10-30");
        Program needCheck = program("P-NEED", "2026-10-10");
        Program notReviewed = program("P-NONE", "2026-10-01");

        when(programRepository.findRecommendationCandidates(
                eq(List.of("경영", "컨설팅", "고용환경개선")),
                any(), eq(null), eq(null), eq(null)))
                .thenReturn(List.of(unmatched, notReviewed, needCheck, matched));
        when(aiReviewRepository.findAllByEmailOrderByCreatedAtDesc("user@example.com"))
                .thenReturn(List.of(
                        review("P-UNMATCHED", AiReviewStatus.UNMATCHED),
                        review("P-NEED", AiReviewStatus.NEED_CHECK),
                        review("P-MATCHED", AiReviewStatus.MATCHED)));

        Page<?> result = service.recommend(
                "user@example.com", "경영", null, null, null, PageRequest.of(0, 10));

        var content = result.getContent();
        assertEquals("P-MATCHED", ((com.miniproject1.miniproject1.program.dto.response.ProgramRecommendationResponseDTO) content.get(0)).getPblancId());
        assertEquals("P-NEED", ((com.miniproject1.miniproject1.program.dto.response.ProgramRecommendationResponseDTO) content.get(1)).getPblancId());
        assertEquals("P-UNMATCHED", ((com.miniproject1.miniproject1.program.dto.response.ProgramRecommendationResponseDTO) content.get(2)).getPblancId());
        assertEquals("P-NONE", ((com.miniproject1.miniproject1.program.dto.response.ProgramRecommendationResponseDTO) content.get(3)).getPblancId());
        assertEquals(100, ((com.miniproject1.miniproject1.program.dto.response.ProgramRecommendationResponseDTO) content.get(0)).getRecommendationScore());
        assertEquals(50, ((com.miniproject1.miniproject1.program.dto.response.ProgramRecommendationResponseDTO) content.get(1)).getRecommendationScore());
        assertEquals(0, ((com.miniproject1.miniproject1.program.dto.response.ProgramRecommendationResponseDTO) content.get(2)).getRecommendationScore());
        assertEquals(null, ((com.miniproject1.miniproject1.program.dto.response.ProgramRecommendationResponseDTO) content.get(3)).getRecommendationScore());
    }

    @Test
    void 더미_AI_조건별_상태가_가중평균_점수로계산된다() {
        Program program = program("P-SCORE", "2026-10-20");
        AiReviewAnalysis analysis = new AiReviewAnalysis(
                List.of(
                        new AiReviewAnalysis.Condition("지역", AiReviewStatus.MATCHED,
                                "서울", "서울", "일치", "서울 소재"),
                        new AiReviewAnalysis.Condition("업종", AiReviewStatus.NEED_CHECK,
                                "서비스", "서비스", "확인 필요", "서비스 대상"),
                        new AiReviewAnalysis.Condition("직원 수", AiReviewStatus.UNMATCHED,
                                "3명", "10명 이상", "불충족", "10명 이상")),
                List.of(), List.of());
        when(programRepository.findRecommendationCandidates(
                eq(List.of("경영", "컨설팅", "고용환경개선")),
                any(), eq(null), eq(null), eq(null)))
                .thenReturn(List.of(program));
        when(aiReviewRepository.findAllByEmailOrderByCreatedAtDesc("user@example.com"))
                .thenReturn(List.of(new AiReview(
                        "user@example.com", "P-SCORE", null,
                        AiReviewStatus.NEED_CHECK, analysis)));

        Page<?> result = service.recommend(
                "user@example.com", "경영", null, null, null, PageRequest.of(0, 10));

        var response = (com.miniproject1.miniproject1.program.dto.response.ProgramRecommendationResponseDTO)
                result.getContent().get(0);
        // (100*25 + 50*20 + 0*10) / (25+20+10) = 63.6 -> 64
        assertEquals(64, response.getRecommendationScore());
    }

    private Program program(String id, String endDate) {
        return Program.builder()
                .pblancId(id)
                .title(id)
                .applyEndDate(java.time.LocalDate.parse(endDate))
                .build();
    }

    private AiReview review(String pblancId, AiReviewStatus status) {
        return new AiReview("user@example.com", pblancId, null, status, null);
    }
}
