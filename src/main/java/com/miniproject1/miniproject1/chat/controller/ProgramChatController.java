package com.miniproject1.miniproject1.chat.controller;

import com.miniproject1.miniproject1.chat.dto.request.ChatRequest;
import com.miniproject1.miniproject1.chat.dto.response.ChatResponse;
import com.miniproject1.miniproject1.chat.service.ProgramChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 현재 보고 있는 공고를 근거로 질문에 답하는 CHAT-01 API입니다. */
@Tag(name = "06. Chat", description = "지원사업 공고 기반 AI 챗봇 API")
@RestController
@RequestMapping("/api/programs/{pblancId}/chat")
@RequiredArgsConstructor
public class ProgramChatController {

    private final ProgramChatService programChatService;

    @Operation(summary = "CHAT-01 공고 기반 AI 챗봇")
    @PostMapping
    public ResponseEntity<ChatResponse> chat(
            Authentication authentication,
            @Parameter(description = "기업마당 공고 ID", required = true)
            @PathVariable(name = "pblancId") String pblancId,
            @Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(programChatService.chat(pblancId, authentication.getName(), request));
    }
}
