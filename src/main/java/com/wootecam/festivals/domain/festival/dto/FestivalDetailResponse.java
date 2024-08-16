package com.wootecam.festivals.domain.festival.dto;

import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.festival.entity.FestivalPublicationStatus;
import java.time.LocalDateTime;

public record FestivalDetailResponse(Long festivalId,
                                     Long adminId,
                                     String title,
                                     String description,
                                     LocalDateTime startTime,
                                     LocalDateTime endTime,
                                     FestivalPublicationStatus festivalPublicationStatus
) {
    public static FestivalDetailResponse from(Festival festival) {
        return new FestivalDetailResponse(
                festival.getId(),
                festival.getAdmin().getId(),
                festival.getTitle(),
                festival.getDescription(),
                festival.getStartTime(),
                festival.getEndTime(),
                festival.getFestivalPublicationStatus()
        );
    }
}
