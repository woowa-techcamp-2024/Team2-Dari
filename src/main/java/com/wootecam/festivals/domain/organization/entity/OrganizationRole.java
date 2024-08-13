package com.wootecam.festivals.domain.organization.entity;

import com.wootecam.festivals.global.docs.EnumType;

public enum OrganizationRole implements EnumType {

    ADMIN("관리자"),
    MEMBER("일반 사용자");

    private final String description;

    OrganizationRole(String description) {
        this.description = description;
    }

    @Override
    public String getName() {
        return this.name();
    }

    public String getDescription() {
        return description;
    }
}
