package com.wootecam.festivals.domain.member.controller;

import com.wootecam.festivals.domain.member.dto.MemberCreateDto;
import com.wootecam.festivals.domain.member.service.MemberService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
    public ResponseEntity<Long> signUpMember(@RequestBody MemberCreateDto memberCreateDto) {
        return new ResponseEntity<>(memberService.createMember(memberCreateDto), HttpStatus.CREATED);
    }

    // 유저 탈퇴
    /*
    기능 확장성을 고려하면 회원 탈퇴할 유저 id 를 받아야함
     */
    @DeleteMapping
    public ResponseEntity<Void> revokeMember(HttpSession session) {
        //TODO 로그인했을 경우 세션에서 가져오는 로직 추가 필요
        Long memberId = (Long) session.getAttribute("memberId");
        memberService.revokeMember(memberId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
