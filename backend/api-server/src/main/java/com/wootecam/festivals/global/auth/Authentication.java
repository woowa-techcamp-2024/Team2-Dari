package com.wootecam.festivals.global.auth;

import com.wootecam.festivals.domain.member.entity.Member;

public record Authentication(Long memberId) {

    public static Authentication from(Member member) {
        return new Authentication(member.getId());
    }
}
