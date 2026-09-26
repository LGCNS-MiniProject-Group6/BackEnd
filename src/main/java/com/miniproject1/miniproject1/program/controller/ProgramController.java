package com.miniproject1.miniproject1.program.controller;

import com.miniproject1.miniproject1.program.service.ProgramSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Program", description = "지원사업 수집 및 DB 저장 API")
@RestController
@RequestMapping("/api/programs")
@RequiredArgsConstructor
public class ProgramController {

    private final ProgramSyncService programSyncService;

    @Operation(summary = "PROGRAM-03 공고 지원사업 목록 DB 저장 (수동 수집 실행)")
    @GetMapping("/Sync")
    public ResponseEntity<String> syncPrograms() {
        System.out.println("debug >>>> ProgramController 호출");
        programSyncService.syncPrograms();
        return ResponseEntity.ok("Open API 공고 데이터 DB 동기화가 완료되었습니다.");
    }
}