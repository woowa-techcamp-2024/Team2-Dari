package com.wootecam.festivals.domain.wait.exception;

import com.wootecam.festivals.global.docs.EnumType;
import com.wootecam.festivals.global.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum WaitErrorCode implements ErrorCode, EnumType {

    INVALID_WAIT_ORDER(HttpStatus.BAD_REQUEST, "WT-0001", "유효하지 않은 대기 번호입니다."),
    QUEUE_EXITED(HttpStatus.BAD_REQUEST, "WT-0002", "대기열에서 이탈하였습니다."),
    ALREADY_WAITING(HttpStatus.BAD_REQUEST, "WT-0003", "이미 대기 중입니다."),
    CANNOT_FOUND_USER(HttpStatus.BAD_REQUEST, "WT-0004", "대기 중인 사용자가 아닙니다."),
    NO_STOCK(HttpStatus.BAD_REQUEST, "WT-0005", "재고가 없습니다."),
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

