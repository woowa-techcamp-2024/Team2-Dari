package com.wootecam.festivals.domain.member.dto;

import com.wootecam.festivals.domain.member.entity.Member;

public record MemberResponse (Long id, String name, String email, String profileImg) {

    public static MemberResponse fromEntity(Member member) {
        return new MemberResponse(member.getId(), member.getName(), member.getEmail(), member.getProfileImg());
    }
}
