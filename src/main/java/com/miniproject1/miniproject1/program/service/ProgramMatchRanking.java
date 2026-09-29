package com.miniproject1.miniproject1.program.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 사업정보와 공고 후보 목록을 비교해 모델이 선정한 적합도 순위입니다.
 * 모델이 긴 pblancId 문자열을 그대로 베껴 쓰다 자릿수를 틀리는 것을 막기 위해,
 * 공고 자체가 아니라 프롬프트에서 부여한 1부터 시작하는 후보 번호(index)로 응답받습니다.
 */
public record ProgramMatchRanking(
        @JsonProperty(required = true) List<RankedProgram> ranked
) {

    /** 공고 하나에 대한 적합도 판단입니다. */
    public record RankedProgram(
            @JsonProperty(required = true) int index,
            @JsonProperty(required = true) int score,
            @JsonProperty(required = true) String reason
    ) {
    }
}
