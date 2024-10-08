package com.wootecam.festivals.domain.festival.dto;

import java.util.List;

public record KeySetPageResponse<T>(List<T> content,
                                    Cursor cursor,
                                    boolean hasNext) {

    public KeySetPageResponse {
        if (!hasNext) {
            cursor = null;
        }
    }
}
