package com.wootecam.festivals.domain.my.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.wootecam.festivals.domain.festival.dto.FestivalAdminResponse;
import com.wootecam.festivals.domain.festival.entity.FestivalProgressStatus;
import com.wootecam.festivals.domain.festival.entity.FestivalPublicationStatus;
import com.wootecam.festivals.global.utils.CustomLocalDateTimeSerializer;
import java.time.LocalDateTime;

public record MyPurchasedFestivalResponse(
        String title,
        String festivalImg,
        @JsonSerialize(using = CustomLocalDateTimeSerializer.class)
        LocalDateTime startTime,
        @JsonSerialize(using = CustomLocalDateTimeSerializer.class)
        LocalDateTime endTime,
        FestivalPublicationStatus festivalPublicationStatus,
        FestivalProgressStatus festivalProgressStatus,
        FestivalAdminResponse admin,
        Long purchaseId,
        @JsonSerialize(using = CustomLocalDateTimeSerializer.class)
        LocalDateTime purchaseTime
){
}
