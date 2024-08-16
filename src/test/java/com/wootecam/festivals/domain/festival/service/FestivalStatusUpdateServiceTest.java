package com.wootecam.festivals.domain.festival.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.festival.entity.FestivalStatus;
import com.wootecam.festivals.domain.festival.repository.FestivalRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class FestivalStatusUpdateServiceTest {

    @Autowired
    private FestivalStatusUpdateService festivalStatusUpdateService;

    @Autowired
    private FestivalRepository festivalRepository;

    @Test
    @DisplayName("updateFestivalStatus 메서드 테스트")
    void testUpdateFestivalStatus() {
        // Given
        Festival festival = Festival.builder()
                .title("Festival 1")
                .description("Description 1")
                .startTime(LocalDateTime.now().plusMinutes(1))
                .endTime(LocalDateTime.now().plusMinutes(2))
                .build();
        Festival savedFestival = festivalRepository.save(festival);

        FestivalStatus newStatus = FestivalStatus.COMPLETED;

        // When
        festivalStatusUpdateService.updateFestivalStatus(savedFestival.getId(), newStatus);

        // Then
        festivalRepository.findById(savedFestival.getId())
                .ifPresent(updatedFestival -> assertEquals(newStatus, updatedFestival.getFestivalStatus()));
    }
}
