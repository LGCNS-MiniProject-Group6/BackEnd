package com.miniproject1.miniproject1.interest.dto.response;

import com.miniproject1.miniproject1.interest.entity.InterestProgram;

import java.time.LocalDateTime;

public record InterestProgramResponse(
        Long id,
        String programId,
        LocalDateTime createdAt
) {
    public static InterestProgramResponse from(InterestProgram interestProgram) {
        return new InterestProgramResponse(
                interestProgram.getId(),
                interestProgram.getProgramId(),
                interestProgram.getCreatedAt());
    }
}
