package com.wootecam.festivals.domain.festival.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.festival.entity.FestivalProgressStatus;
import com.wootecam.festivals.domain.festival.entity.FestivalPublicationStatus;
import com.wootecam.festivals.global.utils.CustomLocalDateTimeSerializer;
import java.time.LocalDateTime;

public record FestivalResponse(Long festivalId,
                               Long adminId,
                               String title,
                               String description,
                               String festivalImg,
                               @JsonSerialize(using = CustomLocalDateTimeSerializer.class)
                               LocalDateTime startTime,
                               @JsonSerialize(using = CustomLocalDateTimeSerializer.class)
                               LocalDateTime endTime,
                               FestivalPublicationStatus festivalPublicationStatus,
                               FestivalProgressStatus festivalProgressStatus
) {
    public static FestivalResponse from(Festival festival) {
        return new FestivalResponse(festival.getId(),
                festival.getAdmin().getId(),
                festival.getTitle(),
                festival.getDescription(),
                festival.getFestivalImg(),
                festival.getStartTime(),
                festival.getEndTime(),
                festival.getFestivalPublicationStatus(),
                festival.getFestivalProgressStatus()
        );
    }
}
