package com.wootecam.festivals.global.api;

import com.wootecam.festivals.global.exception.ErrorCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class ApiErrorResponse {

    private final ResponseStatus status;
    private final ErrorCode errorCode;

    private ApiErrorResponse(ResponseStatus status, ErrorCode errorCode) {
        this.status = status;
        this.errorCode = errorCode;
    }

    public static ApiErrorResponse of(ResponseStatus status, ErrorCode errorCode) {
        return new ApiErrorResponse(status, errorCode);
    }
}
