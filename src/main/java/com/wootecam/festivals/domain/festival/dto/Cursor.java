package com.wootecam.festivals.domain.festival.dto;

import java.time.LocalDateTime;

public record Cursor(LocalDateTime time, Long id) {
}
