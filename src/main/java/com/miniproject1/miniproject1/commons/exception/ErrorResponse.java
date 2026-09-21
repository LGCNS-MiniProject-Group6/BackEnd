package com.miniproject1.miniproject1.commons.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final String code;
    private final String message;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "Asia/Seoul")
    private final ZonedDateTime timestamp;

    private final String path;

    @Builder.Default
    private final List<FieldErrorDetail> details = new ArrayList<>();

    @Getter
    @Builder
    public static class FieldErrorDetail {
        private String field;
        private String value;
        private String reason;
    }

    public static ErrorResponse of(ErrorCode errorCode, String path) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .timestamp(ZonedDateTime.now())
                .path(path)
                .details(new ArrayList<>())
                .build();
    }

    public static ErrorResponse of(ErrorCode errorCode, String path, List<FieldErrorDetail> details) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .timestamp(ZonedDateTime.now())
                .path(path)
                .details(details)
                .build();
    }
}