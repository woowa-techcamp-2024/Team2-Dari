package com.wootecam.festivals.domain.member.controller;

import com.wootecam.festivals.domain.member.dto.MemberCreateDto;
import com.wootecam.festivals.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // 유저 회원가입
    @PostMapping
    public ResponseEntity<?> signUpMember(@RequestBody MemberCreateDto memberCreateDto) {
        //TODO 공통 응답 클래스 필요ㅌ
        return new ResponseEntity<>(memberService.createMember(memberCreateDto), HttpStatus.OK);
    }
}
