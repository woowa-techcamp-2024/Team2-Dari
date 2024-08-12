package com.wootecam.festivals.global.api;

import com.wootecam.festivals.EnumType;

public enum ResponseStatus implements EnumType {

    SUCCESS("성공"),
    FAIL("실패"),
    ERROR("에러"),
    ;

    private final String description;

    ResponseStatus(String description) {
        this.description = description;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public String getDescription() {
        return description;
    }
}
