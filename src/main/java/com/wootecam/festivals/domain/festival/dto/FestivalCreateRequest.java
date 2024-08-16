package com.wootecam.festivals.domain.festival.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record FestivalCreateRequest(
        @NotBlank(message = "축제 제목은 필수입니다.")
        @Size(min = 1, max = 100, message = "축제 제목은 1자 이상 100자 이하여야 합니다.")
        String title,

        @NotBlank(message = "축제 설명은 필수입니다.")
        @Size(max = 1000, message = "축제 설명은 1000자 이하여야 합니다.")
        String description,

        @NotNull(message = "시작 시간은 필수입니다.")
        @Future(message = "시작 시간은 현재보다 미래여야 합니다.")
        LocalDateTime startTime,

        @NotNull(message = "종료 시간은 필수입니다.")
        @Future(message = "종료 시간은 현재보다 미래여야 합니다.")
        LocalDateTime endTime
) {
    @AssertTrue(message = "종료 시간은 시작 시간보다 늦어야 합니다.")
    private boolean isEndTimeAfterStartTime() {
        return endTime != null && startTime != null && endTime.isAfter(startTime);
    }
}
