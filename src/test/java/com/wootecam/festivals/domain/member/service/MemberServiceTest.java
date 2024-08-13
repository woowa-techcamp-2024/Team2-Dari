package com.wootecam.festivals.domain.member.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.wootecam.festivals.domain.member.dto.MemberCreateDto;
import com.wootecam.festivals.domain.member.repository.MemberRepository;
import com.wootecam.festivals.utils.TestDBCleaner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class MemberServiceTest {

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        TestDBCleaner.clear(memberRepository);
    }

    @Test
    @DisplayName("회원가입 테스트")
    void createMember() {
        // given
        String name = "test";
        String email = "test@test.com";
        String profileImg = "test";

        // when
        Long memberId = memberService.createMember(new MemberCreateDto(name, email, profileImg));

        // then
        assertAll(
                () -> assertNotNull(memberId),
                () -> assertEquals(name, memberRepository.findById(memberId).get().getName()),
                () -> assertEquals(email, memberRepository.findById(memberId).get().getEmail()),
                () -> assertEquals(profileImg, memberRepository.findById(memberId).get().getProfileImg())
        );
    }

    @Test
    @DisplayName("회원가입 테스트 - 중복 이메일이 있을 경우 예외 발생")
    void createMemberWithDuplicatedEmail() {
        // given
        String name = "test";
        String email = "test@test.com";
        String profileImg = "test";

        MemberCreateDto memberCreateDto = new MemberCreateDto(name, email, profileImg);
        memberService.createMember(memberCreateDto);

        // when, then
        assertThrows(IllegalArgumentException.class,
                () -> memberService.createMember(memberCreateDto));
    }
}
