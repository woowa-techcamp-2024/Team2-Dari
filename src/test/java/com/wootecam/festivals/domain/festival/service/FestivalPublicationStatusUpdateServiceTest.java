package com.wootecam.festivals.domain.festival.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.festival.entity.FestivalProgressStatus;
import com.wootecam.festivals.domain.festival.repository.FestivalRepository;
import com.wootecam.festivals.domain.member.entity.Member;
import com.wootecam.festivals.domain.member.repository.MemberRepository;
import com.wootecam.festivals.utils.TestDBCleaner;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class FestivalPublicationStatusUpdateServiceTest {

    @Autowired
    private FestivalStatusUpdateService festivalStatusUpdateService;

    @Autowired
    private FestivalRepository festivalRepository;

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        TestDBCleaner.clear(festivalRepository);
        TestDBCleaner.clear(memberRepository);
    }

    @Test
    @DisplayName("updateFestivalStatus 메서드 테스트")
    void testUpdateFestivalStatus() {
        // Given
        Member member = Member.builder()
                .name("test")
                .email("test@test.com")
                .profileImg("")
                .build();
        Member savedMember = memberRepository.save(member);

        Festival festival = Festival.builder()
                .admin(savedMember)
                .title("페스티벌")
                .description("페스티벌 설명")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(2))
                .build();

        Festival savedFestival = festivalRepository.save(festival);

        FestivalProgressStatus newStatus = FestivalProgressStatus.COMPLETED;

        // When
        festivalStatusUpdateService.updateFestivalStatus(savedFestival.getId(), newStatus);

        // Then
        festivalRepository.findById(savedFestival.getId())
                .ifPresent(updatedFestival -> assertEquals(newStatus, updatedFestival.getFestivalPublicationStatus()));
    }
}
