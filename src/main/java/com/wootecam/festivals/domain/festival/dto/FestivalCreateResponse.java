package com.wootecam.festivals.domain.festival.dto;

import com.wootecam.festivals.domain.festival.entity.Festival;

public record FestivalCreateResponse(Long festivalId
) {
    public static FestivalCreateResponse from(Festival festival) {
        return new FestivalCreateResponse(festival.getId());
    }
}
