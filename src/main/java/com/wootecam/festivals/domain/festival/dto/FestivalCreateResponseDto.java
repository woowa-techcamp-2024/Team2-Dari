package com.wootecam.festivals.domain.festival.dto;

import com.wootecam.festivals.domain.festival.entity.Festival;

public record FestivalCreateResponseDto(
        Long festivalId
) {
    public static FestivalCreateResponseDto toResponse(Festival festival) {
        return new FestivalCreateResponseDto(festival.getId());
    }
}
