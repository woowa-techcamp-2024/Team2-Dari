package com.wootecam.festivals.utils;

import com.wootecam.festivals.domain.organization.entity.Organization;

public final class Fixture {

    private Fixture() {
    }

    public static Organization createOrganization(String name, String detail, String profileImg) {
        return Organization.builder()
                .name(name)
                .detail(detail)
                .profileImg(profileImg)
                .build();
    }
}
