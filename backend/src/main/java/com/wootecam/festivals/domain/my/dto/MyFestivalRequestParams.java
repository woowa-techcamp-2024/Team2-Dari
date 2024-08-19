package com.wootecam.festivals.domain.my.dto;

import com.wootecam.festivals.global.constants.GlobalConstants;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

public record MyFestivalRequestParams(@DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
                                      LocalDateTime time,

                                      Long id,

                                      @Min(GlobalConstants.MIN_PAGE_SIZE)
                                      @Max(GlobalConstants.MAX_PAGE_SIZE)
                                      Integer pageSize) {
    public MyFestivalRequestParams {
        if (pageSize == null || pageSize < GlobalConstants.MIN_PAGE_SIZE) {
            pageSize = GlobalConstants.MIN_PAGE_SIZE;
        }
    }
}
