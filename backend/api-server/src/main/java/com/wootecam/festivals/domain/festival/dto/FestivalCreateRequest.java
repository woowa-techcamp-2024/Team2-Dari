package com.wootecam.festivals.domain.festival.dto;


import static com.wootecam.festivals.domain.festival.util.FestivalValidConstant.DESCRIPTION_BLANK_MESSAGE;
import static com.wootecam.festivals.domain.festival.util.FestivalValidConstant.DESCRIPTION_SIZE_MESSAGE;
import static com.wootecam.festivals.domain.festival.util.FestivalValidConstant.END_TIME_AFTER_START_TIME_MESSAGE;
import static com.wootecam.festivals.domain.festival.util.FestivalValidConstant.END_TIME_FUTURE_MESSAGE;
import static com.wootecam.festivals.domain.festival.util.FestivalValidConstant.END_TIME_NULL_MESSAGE;
import static com.wootecam.festivals.domain.festival.util.FestivalValidConstant.MAX_DESCRIPTION_LENGTH;
import static com.wootecam.festivals.domain.festival.util.FestivalValidConstant.MAX_TITLE_LENGTH;
import static com.wootecam.festivals.domain.festival.util.FestivalValidConstant.MIN_TITLE_LENGTH;
import static com.wootecam.festivals.domain.festival.util.FestivalValidConstant.START_TIME_FUTURE_MESSAGE;
import static com.wootecam.festivals.domain.festival.util.FestivalValidConstant.START_TIME_NULL_MESSAGE;
import static com.wootecam.festivals.domain.festival.util.FestivalValidConstant.TITLE_BLANK_MESSAGE;
import static com.wootecam.festivals.domain.festival.util.FestivalValidConstant.TITLE_SIZE_MESSAGE;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.member.entity.Member;
import com.wootecam.festivals.global.utils.CustomLocalDateTimeSerializer;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record FestivalCreateRequest(@NotBlank(message = TITLE_BLANK_MESSAGE)
                                    @Size(min = MIN_TITLE_LENGTH, max = MAX_TITLE_LENGTH, message = TITLE_SIZE_MESSAGE)
                                    String title,

                                    @NotBlank(message = DESCRIPTION_BLANK_MESSAGE)
                                    @Size(max = MAX_DESCRIPTION_LENGTH, message = DESCRIPTION_SIZE_MESSAGE)
                                    String description,

                                    @JsonSerialize(using = CustomLocalDateTimeSerializer.class)
                                    @NotNull(message = START_TIME_NULL_MESSAGE)
                                    @Future(message = START_TIME_FUTURE_MESSAGE)
                                    LocalDateTime startTime,

                                    @JsonSerialize(using = CustomLocalDateTimeSerializer.class)
                                    @NotNull(message = END_TIME_NULL_MESSAGE)
                                    @Future(message = END_TIME_FUTURE_MESSAGE)
                                    LocalDateTime endTime) {

    public Festival toEntity(Member admin) {
        return Festival.builder()
                .admin(admin)
                .title(title)
                .description(description)
                .startTime(startTime)
                .endTime(endTime)
                .build();
    }

    @AssertTrue(message = END_TIME_AFTER_START_TIME_MESSAGE)
    private boolean isEndTimeAfterStartTime() {
        return endTime != null && startTime != null && endTime.isAfter(startTime);
    }
}