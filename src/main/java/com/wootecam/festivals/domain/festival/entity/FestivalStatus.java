package com.wootecam.festivals.domain.festival.entity;

import com.wootecam.festivals.global.docs.EnumType;

public enum FestivalStatus implements EnumType {

    PUBLISHED("공개"),
    DRAFT("비공개"),
    ONGOING("진행중"),
    COMPLETED("종료");

    private final String description;

    FestivalStatus(String description) {
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
