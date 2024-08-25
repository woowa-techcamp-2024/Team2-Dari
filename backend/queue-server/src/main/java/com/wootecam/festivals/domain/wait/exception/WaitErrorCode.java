package com.wootecam.festivals.domain.wait.exception;

import com.wootecam.festivals.global.docs.EnumType;
import com.wootecam.festivals.global.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum WaitErrorCode implements ErrorCode, EnumType {

    INVALID_WAIT_ORDER(HttpStatus.BAD_REQUEST, "WT-0001", "아직 대기열을 통과할 수 없습니다"),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    WaitErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    @Override
    public String getName() {
        return name();
    }

    @Override
    public String getDescription() {
        return httpStatus + ": " + code + " - " + message;
    }
}

