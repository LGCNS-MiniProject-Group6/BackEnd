package com.miniproject1.miniproject1.chat.service;

import com.miniproject1.miniproject1.business.entity.BusinessInfo;
import com.miniproject1.miniproject1.business.repository.BusinessInfoRepository;
import com.miniproject1.miniproject1.chat.client.ProgramChatClient;
import com.miniproject1.miniproject1.chat.dto.request.ChatRequest;
import com.miniproject1.miniproject1.chat.dto.response.ChatResponse;
import com.miniproject1.miniproject1.commons.exception.BusinessException;
import com.miniproject1.miniproject1.commons.exception.ErrorCode;
import com.miniproject1.miniproject1.program.entity.Program;
import com.miniproject1.miniproject1.program.repository.ProgramRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Backend DB의 공고 및 선택적인 사업정보를 근거로 챗봇 답변 생성을 연결합니다. */
@Service
@Transactional(readOnly = true)
public class ProgramChatService {

    static final int MAX_PROGRAM_CONTEXT_LENGTH = 60_000;

    private final ProgramRepository programRepository;
    private final BusinessInfoRepository businessInfoRepository;
    private final ProgramChatClient programChatClient;

    public ProgramChatService(
            ProgramRepository programRepository,
            BusinessInfoRepository businessInfoRepository,
            ProgramChatClient programChatClient) {
        this.programRepository = programRepository;
        this.businessInfoRepository = businessInfoRepository;
        this.programChatClient = programChatClient;
    }

    public ChatResponse chat(String pblancId, String email, ChatRequest request) {
        validatePblancId(pblancId);

        Program program = programRepository.findById(pblancId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROGRAM_NOT_FOUND));
        Optional<BusinessInfo> businessInfo = email == null || email.isBlank()
                ? Optional.empty()
                : businessInfoRepository.findByEmail(email);

        String answer = programChatClient.answer(
                buildProgramContext(program),
                buildBusinessContext(businessInfo),
                request.history(),
                request.message().trim());

        return new ChatResponse(pblancId, answer);
    }

    String buildProgramContext(Program program) {
        String context = """
                공고 ID: %s
                제목: %s
                분야: %s
                주관기관: %s
                지원대상: %s
                사업내용: %s
                원문 신청기간: %s
                신청 시작일: %s
                신청 종료일: %s
                API 갱신일: %s
                """.formatted(
                value(program.getPblancId()),
                value(program.getTitle()),
                value(program.getCategory()),
                value(program.getOrganization()),
                value(program.getTargetDescription()),
                value(program.getDescription()),
                value(program.getRawApplyPeriod()),
                value(program.getApplyStartDate()),
                value(program.getApplyEndDate()),
                value(program.getApiUpdatedAt()));

        if (context.length() <= MAX_PROGRAM_CONTEXT_LENGTH) {
            return context;
        }
        return context.substring(0, MAX_PROGRAM_CONTEXT_LENGTH)
                + "\n[입력 길이 제한으로 이후 내용 생략]";
    }

    String buildBusinessContext(Optional<BusinessInfo> businessInfo) {
        if (businessInfo.isEmpty()) {
            return "등록된 사업정보 없음";
        }

        BusinessInfo business = businessInfo.get();
        return """
                업종: %s
                지역: %s
                개업일: %s
                상시근로자 수: %s
                연 매출: %s
                """.formatted(
                value(business.getIndustry()),
                value(business.getRegion()),
                value(business.getOpeningDate()),
                value(business.getEmployeeCount()),
                value(business.getAnnualRevenue()));
    }

    private String value(Object value) {
        if (value == null || value.toString().isBlank()) {
            return "저장된 정보 없음";
        }
        return value.toString().trim();
    }

    private void validatePblancId(String pblancId) {
        if (pblancId == null || pblancId.isBlank() || pblancId.length() > 255) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
