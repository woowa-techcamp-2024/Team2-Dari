package com.wootecam.festivals.domain.my.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.wootecam.festivals.global.utils.CustomLocalDateTimeSerializer;
import java.time.LocalDateTime;

public record MyFestivalResponse(Long festivalId, String title, String festivalImg,
                                 @JsonSerialize(using = CustomLocalDateTimeSerializer.class) LocalDateTime startTime) {
}
