package com.wootecam.festivals.domain.auth.exception;

import com.wootecam.festivals.global.docs.EnumType;
import com.wootecam.festivals.global.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AuthErrorCode implements ErrorCode, EnumType {

    USER_LOGIN_FAILED(HttpStatus.BAD_REQUEST, "AU-0001", "로그인에 실패하였습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AU-0002", "인증되지 않은 사용자입니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    AuthErrorCode(HttpStatus httpStatus, String code, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
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
