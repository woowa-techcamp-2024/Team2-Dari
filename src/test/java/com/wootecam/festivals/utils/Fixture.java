package com.wootecam.festivals.utils;

import com.wootecam.festivals.domain.member.entity.Member;
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

    public static Member createMember(String name, String email) {
        return Member.builder()
                .name("Test User")
                .email("test@example.com")
                .build();
    }
}
