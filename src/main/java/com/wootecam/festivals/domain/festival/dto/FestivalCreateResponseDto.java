package com.wootecam.festivals.domain.festival.dto;

import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.festival.exception.FestivalErrorCode;
import com.wootecam.festivals.global.exception.type.ApiException;

public record FestivalCreateResponseDto(
        Long festivalId
) {
    public static FestivalCreateResponseDto from(Festival festival) {
        validateFestival(festival);
        return new FestivalCreateResponseDto(festival.getId());
    }

    private static void validateFestival(Festival festival) {
        if (festival == null) {
            throw new ApiException(FestivalErrorCode.InvalidFestivalDataException, "Festival 객체가 null입니다.");
        }
        if (festival.getId() == null) {
            throw new ApiException(FestivalErrorCode.InvalidFestivalDataException, "Festival Id가 null입니다.");
        }
    }
}
