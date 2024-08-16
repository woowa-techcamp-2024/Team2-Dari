package com.wootecam.festivals.utils;

import com.wootecam.festivals.domain.member.entity.Member;

public final class Fixture {

    private Fixture() {
    }

    public static Member createMember(String name, String email) {
        return Member.builder()
                .name("Test User")
                .email("test@example.com")
                .build();
    }
}
