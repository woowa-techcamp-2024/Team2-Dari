package com.wootecam.festivals.domain.ticket.exception;

import com.wootecam.festivals.global.docs.EnumType;
import com.wootecam.festivals.global.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum TicketErrorCode implements ErrorCode, EnumType {

    TICKET_NOT_FOUND(HttpStatus.NOT_FOUND, "TK-0001", "해당하는 티켓을 찾을 수 없습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    TicketErrorCode(HttpStatus httpStatus, String code, String message) {
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
