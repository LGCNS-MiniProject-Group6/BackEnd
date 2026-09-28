package com.miniproject1.miniproject1.interest.controller;

import com.miniproject1.miniproject1.commons.exception.ErrorResponse;
import com.miniproject1.miniproject1.interest.dto.response.InterestProgramResponse;
import com.miniproject1.miniproject1.interest.service.InterestProgramService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class InterestProgramController {

    private final InterestProgramService interestProgramService;

    @Operation(summary = "관심공고 등록")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "등록 성공",
                    content = @Content(schema = @Schema(implementation = InterestProgramResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": 1,
                                      "programId": "PBLN000000000001",
                                      "createdAt": "2026-09-28T16:00:00"
                                    }
                                    """))),
            @ApiResponse(responseCode = "409", description = "이미 등록한 관심공고입니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "FAVORITE_ALREADY_EXISTS",
                                      "message": "이미 등록한 관심공고입니다.",
                                      "timestamp": "2026-09-28T16:00:00+09:00",
                                      "path": "/api/favorites/PBLN000000000001",
                                      "details": []
                                    }
                                    """)))
    })
    @PostMapping("/{pblancId}")
    public ResponseEntity<InterestProgramResponse> add(
            Authentication authentication,
            @PathVariable("pblancId") String programId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(interestProgramService.add(authentication.getName(), programId));
    }

    @Operation(summary = "내 관심공고 조회")
    @GetMapping
    public ResponseEntity<List<InterestProgramResponse>> findAll(Authentication authentication) {
        return ResponseEntity.ok(interestProgramService.findAll(authentication.getName()));
    }

    @Operation(summary = "관심공고 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "등록된 관심공고가 없습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "DATA_NOT_FOUND",
                                      "message": "등록된 관심공고가 없습니다.",
                                      "timestamp": "2026-09-28T16:00:00+09:00",
                                      "path": "/api/favorites/PBLN000000000001",
                                      "details": []
                                    }
                                    """)))
    })
    @DeleteMapping("/{pblancId}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable("pblancId") String programId) {
        interestProgramService.delete(authentication.getName(), programId);
        return ResponseEntity.noContent().build();
    }
}
