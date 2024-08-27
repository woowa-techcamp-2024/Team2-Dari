package com.wootecam.festivals.domain.wait.exception;

import com.wootecam.festivals.global.docs.EnumType;
import com.wootecam.festivals.global.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum WaitErrorCode implements ErrorCode, EnumType {

    INVALID_WAIT_ORDER(HttpStatus.BAD_REQUEST, "WT-0001", "유효하지 않은 대기 번호입니다."),
    ALREADY_WAITING(HttpStatus.BAD_REQUEST, "WT-0002", "이미 대기 중입니다."),
    NEED_WAITING(HttpStatus.BAD_REQUEST, "WT-0003", "대기 중인 사용자가 아닙니다."),
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

