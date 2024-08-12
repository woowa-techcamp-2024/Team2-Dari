package com.wootecam.festivals.domain.member.dto;

import com.wootecam.festivals.domain.member.entity.Member;

public record MemberCreateDto(String memberName, String email, String profileImg) {
    public Member toEntity() {
        return Member.builder()
                .memberName(this.memberName)
                .email(this.email)
                .profileImg(this.profileImg)
                .build();
    }
}
