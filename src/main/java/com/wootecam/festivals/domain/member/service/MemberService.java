package com.wootecam.festivals.domain.member.service;

import com.wootecam.festivals.domain.member.dto.MemberCreateRequest;
import com.wootecam.festivals.domain.member.dto.MemberIdResponse;
import com.wootecam.festivals.domain.member.dto.MemberResponse;
import com.wootecam.festivals.domain.member.entity.Member;
import com.wootecam.festivals.domain.member.exception.MemberErrorCode;
import com.wootecam.festivals.domain.member.repository.MemberRepository;
import com.wootecam.festivals.global.exception.type.ApiException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 회원 관련 비즈니스 로직을 처리하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    /**
     * 새로운 회원을 생성합니다.
     *
     * @param memberCreateRequest 회원 생성 요청 DTO
     * @return 생성된 회원의 ID
     * @throws ApiException 이미 존재하는 이메일인 경우
     */
    @Transactional
    public MemberIdResponse createMember(MemberCreateRequest memberCreateRequest) {
        memberRepository.findByEmail(memberCreateRequest.email())
                .ifPresent(member -> {
                    throw new ApiException(MemberErrorCode.DUPLICATED_EMAIL);
                });

        Member newMember = memberRepository.save(memberCreateRequest.toEntity());
        log.debug("새로운 회원 생성: memberId={}, email={}", newMember.getId(), newMember.getEmail());
        return new MemberIdResponse(newMember.getId());
    }

    /**
     * 회원을 탈퇴 처리합니다.
     *
     * @param memberId 탈퇴할 회원의 ID
     * @throws ApiException 회원을 찾을 수 없는 경우
     */
    @Transactional
    public void withdrawMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ApiException(MemberErrorCode.USER_NOT_FOUND));

        member.updateStatusDeleted();
        log.debug("회원 탈퇴 처리: memberId={}", memberId);
    }

    /**
     * 회원 정보를 조회합니다.
     *
     * @param memberId 조회할 회원의 ID
     * @return 회원 정보 DTO
     * @throws ApiException 회원을 찾을 수 없는 경우
     */
    public MemberResponse findMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ApiException(MemberErrorCode.USER_NOT_FOUND));
        return MemberResponse.from(member);
    }
}