package com.wootecam.global.api;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class ApiErrorResponse {

    private final String errorCode;
    private final String message;

    private ApiErrorResponse(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    public static ApiErrorResponse of(String errorCode, String message) {
        return new ApiErrorResponse(errorCode, message);
    }
}
