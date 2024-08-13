package com.wootecam.festivals.global.exception.type;

import com.wootecam.festivals.global.exception.ErrorCode;

public class DataNotFoundException extends ApiException {

    public DataNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public DataNotFoundException(ErrorCode errorCode, String errorDescription) {
        super(errorCode, errorDescription);
    }

    public DataNotFoundException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }

    public DataNotFoundException(ErrorCode errorCode, String errorDescription, Throwable cause) {
        super(errorCode, errorDescription, cause);
    }
}
