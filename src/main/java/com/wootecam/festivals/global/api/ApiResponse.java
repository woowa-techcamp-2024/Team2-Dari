package com.wootecam.festivals.global.api;

import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@ToString
@Getter
public class ApiResponse<T> {

    private final HttpStatus status;
    private final T data;

    private ApiResponse(HttpStatus status, T data) {
        this.status = status;
        this.data = data;
    }

    public static <T> ApiResponse<T> of(HttpStatus status, T data) {
        return new ApiResponse<>(status, data);
    }
}