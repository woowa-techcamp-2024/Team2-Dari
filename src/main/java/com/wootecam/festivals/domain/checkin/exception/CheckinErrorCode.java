package com.wootecam.festivals.domain.checkin.exception;

import com.wootecam.festivals.global.docs.EnumType;
import com.wootecam.festivals.global.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum CheckinErrorCode implements ErrorCode, EnumType {
    CHECKIN_NOT_FOUND(HttpStatus.NOT_FOUND, "CI-C-0001", "체크인 기록이 존재하지 않습니다."),
    ALREADY_CHECKED_IN(HttpStatus.BAD_REQUEST, "CI-C-0002", "이미 체크인 되었습니다."),
    ALREADY_SAVED_CHECKIN(HttpStatus.CONFLICT, "CI-C-0003", "이미 저장된 체크인 기록이 존재합니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    CheckinErrorCode(HttpStatus httpStatus, String code, String message) {
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
