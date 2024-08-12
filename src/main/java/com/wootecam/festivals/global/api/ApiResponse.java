package com.wootecam.festivals.global.api;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class ApiResponse<T> {

    private final ResponseStatus status = ResponseStatus.SUCCESS;
    private final T data;

    private ApiResponse(T data) {
        this.data = data;
    }

    public static <T> ApiResponse<T> of(T data) {
        return new ApiResponse<>(data);
    }
}