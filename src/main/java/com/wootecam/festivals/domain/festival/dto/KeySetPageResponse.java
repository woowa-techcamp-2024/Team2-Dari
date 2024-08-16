package com.wootecam.festivals.domain.festival.dto;

import java.util.List;

public record KeySetPageResponse<T>(List<T> content,
                                    Long nextCursor,
                                    boolean hasNext) {
}
