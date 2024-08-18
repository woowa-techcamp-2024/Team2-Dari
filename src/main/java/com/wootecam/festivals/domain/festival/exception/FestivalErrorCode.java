package com.wootecam.festivals.domain.festival.exception;

import com.wootecam.festivals.global.docs.EnumType;
import com.wootecam.festivals.global.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum FestivalErrorCode implements ErrorCode, EnumType {

    // 클라이언트 오류
    INVALID_FESTIVAL_DATA(HttpStatus.BAD_REQUEST, "FS-0001", "Festival 데이터에 문제가 있습니다."),
    FESTIVAL_NOT_FOUND(HttpStatus.BAD_REQUEST, "FS-0002", "Festival이 존재하지 않습니다."),
    FESTIVAL_NOT_AUTHORIZED(HttpStatus.FORBIDDEN, "FS-0003", "Festival에 대한 권한이 없습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    FestivalErrorCode(HttpStatus httpStatus, String code, String message) {
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
