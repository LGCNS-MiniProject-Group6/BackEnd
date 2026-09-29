package com.miniproject1.miniproject1.chat.client;

import com.miniproject1.miniproject1.chat.dto.request.ChatHistoryMessage;
import java.util.List;

/** 저장된 공고 정보와 질문을 AI 모델에 전달하는 경계입니다. */
public interface ProgramChatClient {

    String answer(
            String programContext,
            String businessContext,
            List<ChatHistoryMessage> history,
            String question);
}
