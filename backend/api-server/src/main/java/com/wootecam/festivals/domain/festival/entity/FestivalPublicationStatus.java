package com.wootecam.festivals.domain.festival.entity;

import com.wootecam.festivals.global.docs.EnumType;

public enum FestivalPublicationStatus implements EnumType {

    PUBLISHED("공개"),
    DRAFT("비공개"),
    ;

    private final String description;

    FestivalPublicationStatus(String description) {
        this.description = description;
    }

    @Override
    public String getName() {
        return this.name();
    }

    @Override
    public String getDescription() {
        return description;
    }
}
