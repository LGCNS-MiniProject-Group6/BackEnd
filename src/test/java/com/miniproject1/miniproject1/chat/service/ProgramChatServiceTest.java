package com.miniproject1.miniproject1.chat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.miniproject1.miniproject1.business.entity.BusinessInfo;
import com.miniproject1.miniproject1.business.repository.BusinessInfoRepository;
import com.miniproject1.miniproject1.chat.client.ProgramChatClient;
import com.miniproject1.miniproject1.chat.dto.request.ChatRequest;
import com.miniproject1.miniproject1.chat.dto.response.ChatResponse;
import com.miniproject1.miniproject1.commons.exception.BusinessException;
import com.miniproject1.miniproject1.commons.exception.ErrorCode;
import com.miniproject1.miniproject1.program.entity.Program;
import com.miniproject1.miniproject1.program.repository.ProgramRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProgramChatServiceTest {

    @Mock
    private ProgramRepository programRepository;

    @Mock
    private BusinessInfoRepository businessInfoRepository;

    @Mock
    private ProgramChatClient programChatClient;

    private ProgramChatService programChatService;

    @BeforeEach
    void setUp() {
        programChatService = new ProgramChatService(
                programRepository,
                businessInfoRepository,
                programChatClient);
    }

    @Test
    void returnsAnswerUsingStoredProgramAndBusinessInfo() {
        Program program = program("PBLN_001", "소상공인 지원사업", "사업 설명", LocalDate.of(2026, 10, 1));
        BusinessInfo business = BusinessInfo.builder()
                .email("owner@example.com")
                .industry("카페")
                .region("서울")
                .openingDate(LocalDate.of(2024, 3, 15))
                .employeeCount(1)
                .annualRevenue(80_000_000L)
                .build();
        when(programRepository.findById("PBLN_001")).thenReturn(Optional.of(program));
        when(businessInfoRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(business));
        when(programChatClient.answer(
                contains("소상공인 지원사업"),
                contains("업종: 카페"),
                anyList(),
                eq("제가 신청 가능한가요?")))
                .thenReturn("현재 저장된 조건을 기준으로 일부 조건을 비교할 수 있습니다.");

        ChatResponse response = programChatService.chat(
                "PBLN_001",
                "owner@example.com",
                new ChatRequest(" 제가 신청 가능한가요? ", List.of()));

        assertThat(response.pblancId()).isEqualTo("PBLN_001");
        assertThat(response.answer()).contains("일부 조건");
    }

    @Test
    void worksWithoutBusinessInfo() {
        Program program = program("PBLN_001", "지원사업", "설명", null);
        when(programRepository.findById("PBLN_001")).thenReturn(Optional.of(program));
        when(businessInfoRepository.findByEmail("owner@example.com")).thenReturn(Optional.empty());
        when(programChatClient.answer(
                contains("지원사업"),
                eq("등록된 사업정보 없음"),
                anyList(),
                eq("신청기간은?")))
                .thenReturn("현재 저장된 신청기간을 확인해주세요.");

        ChatResponse response = programChatService.chat(
                "PBLN_001",
                "owner@example.com",
                new ChatRequest("신청기간은?", List.of()));

        assertThat(response.answer()).isNotBlank();
    }

    @Test
    void returnsProgramNotFoundBeforeCallingOpenAi() {
        when(programRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> programChatService.chat(
                "UNKNOWN",
                "owner@example.com",
                new ChatRequest("질문", List.of())))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PROGRAM_NOT_FOUND));

        verify(programChatClient, never()).answer(eq(""), eq(""), anyList(), eq("질문"));
    }

    @Test
    void handlesNullDatesAndBlankDescriptionWithoutFakeData() {
        Program program = program("PBLN_001", "지원사업", "", null);

        String context = programChatService.buildProgramContext(program);

        assertThat(context).contains("사업내용: 저장된 정보 없음");
        assertThat(context).contains("신청 종료일: 저장된 정보 없음");
    }

    @Test
    void propagatesOpenAiErrorAsBackendBusinessError() {
        Program program = program("PBLN_001", "지원사업", "설명", null);
        when(programRepository.findById("PBLN_001")).thenReturn(Optional.of(program));
        when(businessInfoRepository.findByEmail("owner@example.com")).thenReturn(Optional.empty());
        when(programChatClient.answer(anyString(), eq("등록된 사업정보 없음"), anyList(), eq("질문")))
                .thenThrow(new BusinessException(ErrorCode.EXTERNAL_API_ERROR));

        assertThatThrownBy(() -> programChatService.chat(
                "PBLN_001",
                "owner@example.com",
                new ChatRequest("질문", List.of())))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.EXTERNAL_API_ERROR));
    }

    private Program program(String id, String title, String description, LocalDate endDate) {
        return Program.builder()
                .pblancId(id)
                .title(title)
                .category("경영")
                .organization("지원기관")
                .targetDescription("소상공인")
                .description(description)
                .rawApplyPeriod(null)
                .applyStartDate(null)
                .applyEndDate(endDate)
                .apiUpdatedAt(null)
                .build();
    }
}
