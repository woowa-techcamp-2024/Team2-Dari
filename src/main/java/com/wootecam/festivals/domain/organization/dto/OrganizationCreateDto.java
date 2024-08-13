package com.wootecam.festivals.domain.organization.dto;

import com.wootecam.festivals.domain.organization.entity.Organization;

public record OrganizationCreateDto(String name,
                                    String detail,
                                    String profileImg) {

    public Organization toEntity() {
        return Organization.builder()
                .name(this.name())
                .detail(this.detail())
                .profileImg(this.profileImg())
                .build();
    }
}
