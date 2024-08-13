package com.wootecam.festivals.domain.member.dto;

import com.wootecam.festivals.domain.member.entity.Member;

public record MemberCreateRequestDto(String name, String email, String profileImg) {
    public Member toEntity() {
        return Member.builder()
                .name(this.name)
                .email(this.email)
                .profileImg(this.profileImg)
                .build();
    }
}
