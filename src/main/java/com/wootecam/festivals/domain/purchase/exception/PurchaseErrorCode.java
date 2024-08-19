package com.wootecam.festivals.domain.purchase.exception;

import com.wootecam.festivals.global.docs.EnumType;
import com.wootecam.festivals.global.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum PurchaseErrorCode implements ErrorCode, EnumType {

    INVALID_TICKET_PURCHASE_TIME(HttpStatus.NOT_FOUND, "TK-0001", "해당 티켓을 구매할 수 있는 시간이 아닙니다."),
    ALREADY_PURCHASED_TICKET(HttpStatus.BAD_REQUEST, "TK-0002", "이미 구매한 티켓입니다."),
    PURCHASE_NOT_FOUND(HttpStatus.NOT_FOUND, "TK-0003", "구매 내역을 찾을 수 없습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    PurchaseErrorCode(HttpStatus httpStatus, String code, String message) {
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
