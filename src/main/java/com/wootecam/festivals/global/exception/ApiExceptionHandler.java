package com.wootecam.festivals.global.exception;

import com.wootecam.festivals.global.api.ApiErrorResponse;
import com.wootecam.festivals.global.exception.type.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.wootecam.festivals.domain")
@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE)
public class ApiExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(ApiException exception) {
        ApiErrorResponse errorResponse = ApiErrorResponse.of(exception.getErrorCode().getCode(),
                exception.getErrorDescription());

        log.error("{}", errorResponse);

        return ResponseEntity
                .status(exception.getErrorCode().getHttpStatus())
                .body(errorResponse);
    }
}