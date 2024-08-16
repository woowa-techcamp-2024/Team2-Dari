package com.wootecam.festivals.domain.festival.dto;

import com.wootecam.festivals.domain.member.entity.Member;

public record FestivalAdminResponse(Long adminId,
                                    String name,
                                    String email,
                                    String profileImg) {
    public static FestivalAdminResponse from(Member member) {
        return new FestivalAdminResponse(member.getId(),
                member.getName(),
                member.getEmail(),
                member.getProfileImg());
    }
}