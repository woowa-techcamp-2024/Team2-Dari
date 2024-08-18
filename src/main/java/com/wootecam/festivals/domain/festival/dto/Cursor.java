package com.wootecam.festivals.domain.festival.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.wootecam.festivals.global.utils.CustomLocalDateTimeSerializer;
import java.time.LocalDateTime;

public record Cursor(@JsonSerialize(using = CustomLocalDateTimeSerializer.class) LocalDateTime time,
                     Long id) {
}
