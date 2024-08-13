package com.wootecam.festivals.global.exception;

import com.wootecam.festivals.global.api.ApiErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.wootecam.festivals.domain")
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(Exception exception) {
        ApiErrorResponse errorResponse = ApiErrorResponse.of(GlobalErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                "서비스 장애가 발생했습니다.");

        log.error("{}", errorResponse);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiErrorResponse> handleBindException(BindException exception) {
        String errorMessage = exception.getBindingResult().getAllErrors().get(0).getDefaultMessage();

        ApiErrorResponse errorResponse = ApiErrorResponse.of(GlobalErrorCode.INVALID_REQUEST_PARAMETER.getCode(),
                errorMessage);

        log.error("{}", errorResponse);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }
}