package com.wootecam.festivals.domain.member.controller;

import com.wootecam.festivals.domain.member.dto.MemberCreateRequest;
import com.wootecam.festivals.domain.member.dto.MemberIdResponse;
import com.wootecam.festivals.domain.member.dto.MemberResponse;
import com.wootecam.festivals.domain.member.service.MemberService;
import com.wootecam.festivals.global.api.ApiResponse;
import com.wootecam.festivals.global.auth.AuthUser;
import com.wootecam.festivals.global.auth.Authentication;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 회원 관련 API를 처리하는 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /**
     * 회원 가입 API
     *
     * @param memberCreateRequest 회원 생성 요청 DTO
     * @return 생성된 회원의 ID
     */
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MemberIdResponse> signUpMember(@Valid @RequestBody MemberCreateRequest memberCreateRequest) {
        log.debug("회원 가입 요청");
        MemberIdResponse response = memberService.createMember(memberCreateRequest);
        log.debug("회원 가입 완료 - 회원 ID: {}", response.id());
        return ApiResponse.of(response);
    }

    /**
     * 회원 탈퇴 API
     *
     * @param loginMember 로그인한 회원 정보
     * @return 탈퇴한 회원의 ID
     */
    @DeleteMapping
    public ApiResponse<MemberIdResponse> withdrawMember(@AuthUser Authentication loginMember) {
        Long loginMemberId = loginMember.memberId();
        log.debug("회원 탈퇴 요청 - 회원 ID: {}", loginMemberId);
        memberService.withdrawMember(loginMemberId);
        log.debug("회원 탈퇴 완료 - 회원 ID: {}", loginMemberId);
        return ApiResponse.of(new MemberIdResponse(loginMemberId));
    }

    /**
     * 회원 정보 조회 API
     *
     * @param memberId 조회할 회원의 ID
     * @return 조회된 회원 정보
     */
    @GetMapping("/{memberId}")
    public ApiResponse<MemberResponse> findMember(@PathVariable Long memberId) {
        log.debug("회원 정보 조회 요청 - 회원 ID: {}", memberId);
        MemberResponse response = memberService.findMember(memberId);
        log.debug("회원 정보 조회 완료 - 회원 ID: {}", memberId);
        return ApiResponse.of(response);
    }
}