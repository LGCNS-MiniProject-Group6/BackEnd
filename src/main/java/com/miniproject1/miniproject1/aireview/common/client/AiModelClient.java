package com.miniproject1.miniproject1.aireview.common.client;

import com.miniproject1.miniproject1.aireview.common.prompt.AiPrompt;

/** AI 검수에서 사용하는 구조화 모델 호출 규격입니다. */
public interface AiModelClient {

    /** 정해진 검수 응답 객체로 모델 결과를 변환합니다. */
    <T> T generateStructured(AiPrompt prompt, Class<T> responseType);
}
