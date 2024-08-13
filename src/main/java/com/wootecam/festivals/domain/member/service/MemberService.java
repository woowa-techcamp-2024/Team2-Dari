package com.wootecam.festivals.domain.member.service;

import com.wootecam.festivals.domain.member.dto.MemberCreateDto;
import com.wootecam.festivals.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public Long createMember(MemberCreateDto memberCreateDto) {

        memberRepository.findByEmail(memberCreateDto.email())
                .ifPresent(member -> {
                    throw new IllegalArgumentException("이미 존재하는 회원입니다.");
                });

        return memberRepository
                .save(memberCreateDto.toEntity())
                .getId();
    }
}
