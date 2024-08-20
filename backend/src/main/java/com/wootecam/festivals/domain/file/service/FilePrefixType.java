package com.wootecam.festivals.domain.file.service;

import com.wootecam.festivals.global.docs.EnumType;

public enum FilePrefixType implements EnumType {

    MEMBER("member"),
    FESTIVAL("festival");

    private final String description;

    FilePrefixType(String description) {
        this.description = description;
    }

    @Override
    public String getName() {
        return this.name();
    }

    @Override
    public String getDescription() {
        return this.description;
    }
}
