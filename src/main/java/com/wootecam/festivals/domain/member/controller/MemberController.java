package com.wootecam.festivals.domain.member.controller;

import com.wootecam.festivals.domain.member.dto.MemberCreateRequestDto;
import com.wootecam.festivals.domain.member.dto.MemberIdResponseDto;
import com.wootecam.festivals.domain.member.service.MemberService;
import com.wootecam.festivals.global.api.ApiResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // 유저 회원가입
    @PostMapping
    public ApiResponse<MemberIdResponseDto> signUpMember(@RequestBody MemberCreateRequestDto memberCreateRequestDto) {
        return ApiResponse.of(new MemberIdResponseDto(memberService.createMember(memberCreateRequestDto)));
    }

    // 유저 탈퇴
    /*
    기능 확장성을 고려하면 회원 탈퇴할 유저 id 를 받아야함
     */
    @DeleteMapping
    public ApiResponse<MemberIdResponseDto> withdrawMember(HttpSession session) {
        //TODO 로그인했을 경우 세션에서 가져오는 로직 추가 필요
        Long memberId = (Long) session.getAttribute("memberId");
        memberService.withdrawMember(memberId);
        return ApiResponse.of(new MemberIdResponseDto(memberId));
    }
}
