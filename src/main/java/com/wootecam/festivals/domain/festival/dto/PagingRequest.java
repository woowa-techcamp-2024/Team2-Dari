package com.wootecam.festivals.domain.festival.dto;

import com.wootecam.festivals.global.constants.GlobalConstants;

public record PagingRequest(Integer page, Integer size) {
    public PagingRequest {
        // page가 null이거나 0보다 작은 경우 첫번째 페이지로 초기화
        if (page == null || page < 0) {
            page = 0;
        }

        if (size == null || size < GlobalConstants.MIN_PAGE_SIZE) {
            size = GlobalConstants.MIN_PAGE_SIZE;
        }

        if (size > GlobalConstants.MAX_PAGE_SIZE) {
            size = GlobalConstants.MAX_PAGE_SIZE;
        }
    }
}
