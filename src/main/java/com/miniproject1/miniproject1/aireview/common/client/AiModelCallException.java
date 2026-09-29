package com.miniproject1.miniproject1.aireview.common.client;

import com.miniproject1.miniproject1.commons.exception.BusinessException;
import com.miniproject1.miniproject1.commons.exception.ErrorCode;
import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;
import java.util.concurrent.TimeoutException;

/**
 * AI 외부 호출 실패를 백엔드 공통 오류 형식으로 변환합니다.
 */
public final class AiModelCallException extends BusinessException {

    private AiModelCallException(ErrorCode errorCode) {
        super(errorCode);
    }

    private AiModelCallException(ErrorCode errorCode, Throwable cause) {
        super(errorCode);
        initCause(cause);
    }

    static AiModelCallException emptyResponse() {
        return new AiModelCallException(ErrorCode.EXTERNAL_API_ERROR);
    }

    static AiModelCallException from(Throwable cause) {
        ErrorCode errorCode = isTimeout(cause)
                ? ErrorCode.EXTERNAL_API_TIMEOUT
                : ErrorCode.EXTERNAL_API_ERROR;
        return new AiModelCallException(errorCode, cause);
    }

    private static boolean isTimeout(Throwable throwable) {
        Throwable current = throwable;

        while (current != null) {
            if (current instanceof SocketTimeoutException
                    || current instanceof HttpTimeoutException
                    || current instanceof TimeoutException) {
                return true;
            }

            if (current == current.getCause()) {
                break;
            }
            current = current.getCause();
        }

        return false;
    }
}
