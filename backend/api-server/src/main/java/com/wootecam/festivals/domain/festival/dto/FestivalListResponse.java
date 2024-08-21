package com.wootecam.festivals.domain.festival.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.festival.entity.FestivalProgressStatus;
import com.wootecam.festivals.domain.festival.entity.FestivalPublicationStatus;
import com.wootecam.festivals.global.utils.CustomLocalDateTimeSerializer;
import java.time.LocalDateTime;

public record FestivalListResponse(Long festivalId,
                                   String title,
                                   String festivalImg,
                                   @JsonSerialize(using = CustomLocalDateTimeSerializer.class)
                                   LocalDateTime startTime,
                                   @JsonSerialize(using = CustomLocalDateTimeSerializer.class)
                                   LocalDateTime endTime,
                                   FestivalPublicationStatus festivalPublicationStatus,
                                   FestivalProgressStatus festivalProgressStatus,
                                   FestivalAdminResponse admin) {

    public static FestivalListResponse from(Festival festival) {
        return new FestivalListResponse(festival.getId(),
                festival.getTitle(),
                festival.getFestivalImg(),
                festival.getStartTime(),
                festival.getEndTime(),
                festival.getFestivalPublicationStatus(),
                festival.getFestivalProgressStatus(),
                com.wootecam.festivals.domain.festival.dto.FestivalAdminResponse.from(festival.getAdmin())
        );
    }
}
