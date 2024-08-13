package com.wootecam.festivals.domain.festival.dto;

import com.wootecam.festivals.domain.festival.entity.Festival;
import java.time.LocalDateTime;

public record FestivalCreateRequestDto(
        Long organizationId,
        String title,
        String description,
        LocalDateTime startTime,
        LocalDateTime endTime
) {
    public Festival toEntity() {
        return Festival.builder()
                .organizationId(organizationId)
                .title(title)
                .description(description)
                .startTime(startTime)
                .endTime(endTime)
                .build();
    }
}
