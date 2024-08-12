package com.wootecam.festivals.domain.member.service;

import com.wootecam.festivals.domain.member.dto.MemberCreateDto;
import com.wootecam.festivals.domain.member.entity.Member;
import com.wootecam.festivals.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    public Member createMember(MemberCreateDto memberCreateDto) {
        return memberRepository.save(memberCreateDto.toEntity());
    }

    public Member getMember(Long id) {
        return null;
    }

    public void deleteMember(Long id) {

    }
}
