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
    public ApiResponse<MemberIdResponse> signUpMember(@Valid @RequestBody MemberCreateRequest memberCreateRequest) {
        return ApiResponse.of(new MemberIdResponse(memberService.createMember(memberCreateRequest)));
    }

    // 유저 탈퇴
    @DeleteMapping
    public ApiResponse<MemberIdResponse> withdrawMember(@AuthUser Authentication loginMember) {
        Long loginMemberId = loginMember.memberId();
        memberService.withdrawMember(loginMemberId);
        return ApiResponse.of(new MemberIdResponse(loginMemberId));
    }

    // 유저 정보 조회
    @GetMapping("/{memberId}")
    public ApiResponse<MemberResponse> getMember(@PathVariable Long memberId) {
        return ApiResponse.of(memberService.findMember(memberId));
    }
}
