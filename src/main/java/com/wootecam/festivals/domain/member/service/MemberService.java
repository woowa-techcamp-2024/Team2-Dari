package com.wootecam.festivals.domain.member.service;

import com.wootecam.festivals.domain.member.dto.MemberCreateRequest;
import com.wootecam.festivals.domain.member.dto.MemberResponse;
import com.wootecam.festivals.domain.member.entity.Member;
import com.wootecam.festivals.domain.member.exception.UserErrorCode;
import com.wootecam.festivals.domain.member.repository.MemberRepository;
import com.wootecam.festivals.global.exception.type.ApiException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public Long createMember(MemberCreateRequest memberCreateRequest) {

        memberRepository.findByEmail(memberCreateRequest.email())
                .ifPresent(member -> {
                    throw new ApiException(UserErrorCode.DUPLICATED_EMAIL);
                });

        return memberRepository
                .save(memberCreateRequest.toEntity())
                .getId();
    }

    @Transactional
    public void withdrawMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ApiException(UserErrorCode.USER_NOT_FOUND));

        member.updateStatusDeleted();
    }

    public MemberResponse findMember(Long memberId) {
        return MemberResponse.fromEntity(
                memberRepository.findById(memberId)
                .orElseThrow(() -> new ApiException(UserErrorCode.USER_NOT_FOUND))
        );
    }
}
