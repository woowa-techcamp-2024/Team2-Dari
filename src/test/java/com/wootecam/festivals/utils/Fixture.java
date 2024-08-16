package com.wootecam.festivals.utils;

import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.member.entity.Member;
import java.time.LocalDateTime;

public final class Fixture {

    private Fixture() {
    }

    public static Member createMember(String name, String email) {
        return Member.builder()
                .name(name)
                .email(email)
                .profileImg("https://example.com/test.jpg")
                .build();
    }

    public static Festival createFestival(String title, String description,
                                          LocalDateTime startTime, LocalDateTime endTime) {
        return Festival.builder()
                .title(title)
                .description(description)
                .startTime(startTime)
                .endTime(endTime)
                .build();
    }
}
