package com.miniproject1.miniproject1.program.controller;

import com.miniproject1.miniproject1.program.dto.response.ProgramDetailResponseDTO;
import com.miniproject1.miniproject1.program.dto.response.ProgramResponseDTO;
import com.miniproject1.miniproject1.program.service.ProgramReadService;
import com.miniproject1.miniproject1.program.service.ProgramSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Program", description = "지원사업 공고 수집, 동기화 및 조회 API")
@RestController
@RequestMapping("/api/programs")
@RequiredArgsConstructor
public class ProgramController {

    private final ProgramSyncService programSyncService;
    private final ProgramReadService programReadService;

    @Operation(summary = "PROGRAM-01 공고 통합 검색 및 목록 조회", description = "제목, 카테고리, 기관, 지원대상, 사업내용 필드를 대상으로 통합 검색 및 페이징 목록 조회를 수행합니다.")
    @GetMapping
    public ResponseEntity<Page<ProgramResponseDTO>> getPrograms(
            @RequestParam(name = "keyword", required = false) String keyword,
            @ParameterObject @PageableDefault(size = 10, sort = "applyStartDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ProgramResponseDTO> response = programReadService.searchPrograms(keyword, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "PROGRAM-02 지원사업 상세 조회", description = "공고 ID(pblancId)를 통해 해당 공고의 기본 정보 및 AI 요약 6가지 정보를 함께 조회합니다.")
    @GetMapping("/{pblancId}")
    public ResponseEntity<ProgramDetailResponseDTO> getProgramDetail(
            @Parameter(description = "공고 식별자 ID", example = "PBLN_000000000092578") @PathVariable(name = "pblancId") String pblancId) {
        ProgramDetailResponseDTO response = programReadService.getProgramDetail(pblancId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "PROGRAM-03 공고 데이터 수동 동기화", description = "기업마당 Open API 데이터를 수집하여 DB에 저장 및 업데이트합니다.")
    @GetMapping("/sync")
    public ResponseEntity<String> syncPrograms() {
        System.out.println("debug >>>> ProgramController syncPrograms 호출");
        programSyncService.syncPrograms();
        return ResponseEntity.ok("Open API 공고 데이터 DB 동기화가 완료되었습니다.");
    }

    // @Operation(summary = "PROGRAM-추가 실시간 연관 검색어 조회 (자동완성) - 추후 필요하면 사용", //
    // description = "검색어 입력 시 제목 및 수행기관 기준 최신 공고 Top 5를 추천합니다.") //
    // @GetMapping("/autocomplete") // public
    // ResponseEntity<List<ProgramAutoCompleteResponseDTO>> getAutoComplete( //
    // @RequestParam(name = "keyword") String keyword) { // ★ name = "keyword" 추가 //
    // List<ProgramAutoCompleteResponseDTO> results = //
    // programReadService.getAutoCompleteList(keyword); // return
    // ResponseEntity.ok(results); // }
}
