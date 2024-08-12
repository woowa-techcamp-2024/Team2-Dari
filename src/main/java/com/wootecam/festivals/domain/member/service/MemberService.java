package com.wootecam.festivals.domain.member.service;

import com.wootecam.festivals.domain.member.dto.MemberCreateDto;
import com.wootecam.festivals.domain.member.entity.Member;

public interface MemberService {

    Member createMember(MemberCreateDto dto);

    Member getMember(Long id);

    void deleteMember(Long id);
}
