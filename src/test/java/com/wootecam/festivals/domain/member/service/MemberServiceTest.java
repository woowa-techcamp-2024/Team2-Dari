package com.wootecam.festivals.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.wootecam.festivals.domain.member.dto.MemberCreateDto;
import com.wootecam.festivals.domain.member.entity.Member;
import com.wootecam.festivals.domain.member.exception.UserErrorCode;
import com.wootecam.festivals.domain.member.repository.MemberRepository;
import com.wootecam.festivals.global.exception.type.ApiException;
import com.wootecam.festivals.utils.TestDBCleaner;
import java.util.Optional;
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
        ApiException ex = assertThrows(ApiException.class,
                () -> memberService.createMember(memberCreateDto));
        assertEquals(ex.getErrorCode(), UserErrorCode.DUPLICATED_EMAIL);
    }

    @Test
    @DisplayName("회원 탈퇴를 한 유저는 조회되어야하며 삭제 상태여야한다")
    void revokeMember() {
        // given
        String name = "test";
        String email = "test@test.com";
        String profileImg = "test";

        Long memberId = memberService.createMember(new MemberCreateDto(name, email, profileImg));// 가입한 유저가 존재할 때

        // when
        memberService.revokeMember(memberId);

        // then
        Optional<Member> member = memberRepository.findById(memberId);
        assertTrue(member.isPresent());
        assertTrue(member.get().isDeleted());
    }

    @Test
    @DisplayName("회원 탈퇴를 하고자 하는 유저가 디비에 없을 때 404 에러가 발생한다")
    void revokeMemberWithNotExistMember() {
        // given
        Long notExistMemberId = 1L;

        // when
        ApiException exception = assertThrows(ApiException.class,
                () -> memberService.revokeMember(notExistMemberId));

        // then
        assertThat(exception.getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
    }
}
