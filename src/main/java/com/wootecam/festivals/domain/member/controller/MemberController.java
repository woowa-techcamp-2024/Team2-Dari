package com.wootecam.festivals.domain.member.controller;

import com.wootecam.festivals.domain.member.dto.MemberCreateRequestDto;
import com.wootecam.festivals.domain.member.dto.MemberIdResponseDto;
import com.wootecam.festivals.domain.member.dto.MemberResponse;
import com.wootecam.festivals.domain.member.service.MemberService;
import com.wootecam.festivals.global.api.ApiResponse;
import com.wootecam.festivals.global.auth.AuthUser;
import com.wootecam.festivals.global.auth.Authentication;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // 유저 회원가입
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MemberIdResponseDto> signUpMember(@RequestBody MemberCreateRequestDto memberCreateRequestDto) {
        return ApiResponse.of(new MemberIdResponseDto(memberService.createMember(memberCreateRequestDto)));
    }

    // 유저 탈퇴
    /*
    기능 확장성을 고려하면 회원 탈퇴할 유저 id 를 받아야함
     */
    @DeleteMapping
    public ApiResponse<MemberIdResponseDto> withdrawMember(@AuthUser Authentication loginMember) {
        Long loginMemberId = loginMember.memberId();
        memberService.withdrawMember(loginMemberId);
        return ApiResponse.of(new MemberIdResponseDto(loginMemberId));
    }

    // 유저 정보 조회
    @GetMapping("/{memberId}")
    public ApiResponse<MemberResponse> getMember(@PathVariable Long memberId) {
        return ApiResponse.of(memberService.findMember(memberId));
    }
}
