package com.wootecam.festivals.domain.my.dto;

import java.time.LocalDateTime;

public record MyFestivalResponse(Long festivalId, String title, LocalDateTime startTime) {
}
