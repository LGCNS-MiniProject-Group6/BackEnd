package com.miniproject1.miniproject1.program.service;

import com.miniproject1.miniproject1.program.dto.response.ProgramAutoCompleteResponseDTO;
import com.miniproject1.miniproject1.program.dto.response.ProgramDetailResponseDTO;
import com.miniproject1.miniproject1.program.dto.response.ProgramResponseDTO;
import com.miniproject1.miniproject1.program.entity.Program;
import com.miniproject1.miniproject1.program.repository.ProgramRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProgramReadService {

    private final ProgramRepository programRepository;

    // 단건 상세 조회
    public ProgramResponseDTO getProgramById(String pblancId) {
        Program program = programRepository.findById(pblancId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공고 ID입니다: " + pblancId));
        return ProgramResponseDTO.from(program);
    }

    // 1. 상단 연관 검색어 (Top 5)
    public List<ProgramAutoCompleteResponseDTO> getAutoCompleteList(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        Pageable topFive = PageRequest.of(0, 5);
        return programRepository.findAutoCompleteTop5(keyword.trim(), topFive)
                .stream()
                .map(ProgramAutoCompleteResponseDTO::from)
                .toList();
    }

    // 2. 하단 공고 목록 통합 검색 (키워드 미입력 시 전체 목록 조회)
    public Page<ProgramResponseDTO> searchPrograms(String keyword, String category, Pageable pageable) {
        String cleanKeyword = (keyword != null) ? keyword.trim() : "";
        String cleanCategory = (category != null) ? category.trim() : "";
        return programRepository.searchIntegrated(cleanKeyword, cleanCategory, pageable)
                .map(ProgramResponseDTO::from);
    }

    /**
     * PROGRAM-02: 지원사업 상세 조회
     */
    public ProgramDetailResponseDTO getProgramDetail(String pblancId) {
        Program program = programRepository.findById(pblancId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공고 ID입니다: " + pblancId));

        // Program 엔티티의 연관관계를 활용하여 AI 요약 정보 수신 (없으면 null 처리)
        return ProgramDetailResponseDTO.of(program, program.getAiSummary());
    }
}