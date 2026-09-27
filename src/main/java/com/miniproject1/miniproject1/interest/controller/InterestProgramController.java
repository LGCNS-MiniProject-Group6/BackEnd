package com.miniproject1.miniproject1.interest.controller;

import com.miniproject1.miniproject1.interest.dto.response.InterestProgramResponse;
import com.miniproject1.miniproject1.interest.service.InterestProgramService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Interest", description = "관심공고 관리 API")
@RestController
@RequestMapping("/api/interests/programs")
@RequiredArgsConstructor
public class InterestProgramController {

    private final InterestProgramService interestProgramService;

    @Operation(summary = "관심공고 등록")
    @PostMapping("/{programId}")
    public ResponseEntity<InterestProgramResponse> add(
            Authentication authentication,
            @PathVariable String programId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(interestProgramService.add(authentication.getName(), programId));
    }

    @Operation(summary = "내 관심공고 조회")
    @GetMapping
    public ResponseEntity<List<InterestProgramResponse>> findAll(Authentication authentication) {
        return ResponseEntity.ok(interestProgramService.findAll(authentication.getName()));
    }

    @Operation(summary = "관심공고 삭제")
    @DeleteMapping("/{programId}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable String programId) {
        interestProgramService.delete(authentication.getName(), programId);
        return ResponseEntity.noContent().build();
    }
}
