package com.wootecam.festivals.domain.my.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.wootecam.festivals.global.utils.CustomLocalDateTimeSerializer;
import java.time.LocalDateTime;

public record MyFestivalCursor(@JsonSerialize(using = CustomLocalDateTimeSerializer.class) LocalDateTime startTime,
                               Long id) {
}
