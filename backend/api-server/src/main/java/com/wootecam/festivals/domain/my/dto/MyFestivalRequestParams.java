package com.wootecam.festivals.domain.my.dto;

import com.wootecam.festivals.global.constants.GlobalConstants;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

public record MyFestivalRequestParams(@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
                                      LocalDateTime time,
                                      Long id,
                                      Integer pageSize) {
    public MyFestivalRequestParams {
        if (pageSize == null || pageSize < GlobalConstants.MIN_PAGE_SIZE) {
            pageSize = GlobalConstants.MIN_PAGE_SIZE;
        }

        if (pageSize > GlobalConstants.MAX_PAGE_SIZE) {
            pageSize = GlobalConstants.MAX_PAGE_SIZE;
        }
    }
}
