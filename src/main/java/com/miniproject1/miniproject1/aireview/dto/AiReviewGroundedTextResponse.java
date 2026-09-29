package com.miniproject1.miniproject1.aireview.dto;

import com.miniproject1.miniproject1.aireview.AiReviewAnalysis.GroundedText;

public record AiReviewGroundedTextResponse(String text, String evidence) {

    public static AiReviewGroundedTextResponse from(GroundedText groundedText) {
        return new AiReviewGroundedTextResponse(groundedText.text(), groundedText.evidence());
    }
}
