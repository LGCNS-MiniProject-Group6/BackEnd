package com.miniproject1.miniproject1.chat.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.miniproject1.miniproject1.chat.dto.response.ChatResponse;
import com.miniproject1.miniproject1.chat.service.ProgramChatService;
import com.miniproject1.miniproject1.commons.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class ProgramChatControllerTest {

    private ProgramChatService programChatService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        programChatService = mock(ProgramChatService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ProgramChatController(programChatService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void returnsChatAnswer() throws Exception {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("owner@example.com");
        when(programChatService.chat(eq("PBLN_001"), eq("owner@example.com"), any()))
                .thenReturn(new ChatResponse("PBLN_001", "신청기간은 10월 1일까지입니다."));

        mockMvc.perform(post("/api/programs/{pblancId}/chat", "PBLN_001")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "언제까지 신청해?",
                                  "history": []
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pblancId").value("PBLN_001"))
                .andExpect(jsonPath("$.answer").value("신청기간은 10월 1일까지입니다."));
    }

    @Test
    void rejectsBlankMessage() throws Exception {
        Authentication authentication = mock(Authentication.class);

        mockMvc.perform(post("/api/programs/{pblancId}/chat", "PBLN_001")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message": " ", "history": []}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"));
    }

    @Test
    void rejectsMoreThanTenHistoryMessages() throws Exception {
        Authentication authentication = mock(Authentication.class);

        mockMvc.perform(post("/api/programs/{pblancId}/chat", "PBLN_001")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "질문",
                                  "history": [
                                    {"role":"user","content":"1"},
                                    {"role":"assistant","content":"2"},
                                    {"role":"user","content":"3"},
                                    {"role":"assistant","content":"4"},
                                    {"role":"user","content":"5"},
                                    {"role":"assistant","content":"6"},
                                    {"role":"user","content":"7"},
                                    {"role":"assistant","content":"8"},
                                    {"role":"user","content":"9"},
                                    {"role":"assistant","content":"10"},
                                    {"role":"user","content":"11"}
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"));
    }
}
