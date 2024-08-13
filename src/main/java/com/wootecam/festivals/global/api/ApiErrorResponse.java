package com.wootecam.festivals.global.api;

import com.wootecam.festivals.global.exception.ErrorCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class ApiErrorResponse {

    private final ErrorCode errorCode;

    private ApiErrorResponse(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public static ApiErrorResponse of(ErrorCode errorCode) {
        return new ApiErrorResponse(errorCode);
    }
}
