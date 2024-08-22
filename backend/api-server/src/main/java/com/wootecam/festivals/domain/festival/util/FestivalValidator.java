package com.wootecam.festivals.domain.festival.util;

import static com.wootecam.festivals.domain.festival.util.FestivalValidConstant.ADMIN_NULL_MESSAGE;
import static com.wootecam.festivals.domain.festival.util.FestivalValidConstant.DESCRIPTION_EMPTY_MESSAGE;
import static com.wootecam.festivals.domain.festival.util.FestivalValidConstant.DESCRIPTION_LENGTH_MESSAGE;
import static com.wootecam.festivals.domain.festival.util.FestivalValidConstant.DESCRIPTION_NULL_MESSAGE;
import static com.wootecam.festivals.domain.festival.util.FestivalValidConstant.END_TIME_FUTURE_MESSAGE;
import static com.wootecam.festivals.domain.festival.util.FestivalValidConstant.END_TIME_NULL_MESSAGE;
import static com.wootecam.festivals.domain.festival.util.FestivalValidConstant.MAX_DESCRIPTION_LENGTH;
import static com.wootecam.festivals.domain.festival.util.FestivalValidConstant.MAX_TITLE_LENGTH;
import static com.wootecam.festivals.domain.festival.util.FestivalValidConstant.START_TIME_FUTURE_MESSAGE;
import static com.wootecam.festivals.domain.festival.util.FestivalValidConstant.START_TIME_NULL_MESSAGE;
import static com.wootecam.festivals.domain.festival.util.FestivalValidConstant.TIME_RANGE_MESSAGE;
import static com.wootecam.festivals.domain.festival.util.FestivalValidConstant.TITLE_EMPTY_MESSAGE;
import static com.wootecam.festivals.domain.festival.util.FestivalValidConstant.TITLE_LENGTH_MESSAGE;
import static com.wootecam.festivals.domain.festival.util.FestivalValidConstant.TITLE_NULL_MESSAGE;

import com.wootecam.festivals.domain.festival.entity.FestivalProgressStatus;
import com.wootecam.festivals.domain.member.entity.Member;
import java.time.LocalDateTime;
import java.util.Objects;

public class FestivalValidator {

    private FestivalValidator() {
    }

    public static void validateFestival(Member admin, String title, String description,
                                        LocalDateTime startTime, LocalDateTime endTime) {
        Objects.requireNonNull(admin, ADMIN_NULL_MESSAGE);
        validateTitle(title);
        validateDescription(description);
        validateTimeRange(startTime, endTime);
    }

    public static void validateTitle(String title) {
        Objects.requireNonNull(title, TITLE_NULL_MESSAGE);
        if (title.isEmpty()) {
            throw new IllegalArgumentException(TITLE_EMPTY_MESSAGE);
        }
        if (title.length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException(TITLE_LENGTH_MESSAGE);
        }
    }

    public static void validateDescription(String description) {
        Objects.requireNonNull(description, DESCRIPTION_NULL_MESSAGE);
        if (description.isEmpty()) {
            throw new IllegalArgumentException(DESCRIPTION_EMPTY_MESSAGE);
        }
        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            throw new IllegalArgumentException(DESCRIPTION_LENGTH_MESSAGE);
        }
    }

    public static void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        Objects.requireNonNull(startTime, START_TIME_NULL_MESSAGE);
        Objects.requireNonNull(endTime, END_TIME_NULL_MESSAGE);

        LocalDateTime now = LocalDateTime.now().minusMinutes(1); // 1분의 여유를 둡니다.

        if (startTime.isBefore(now)) {
            throw new IllegalArgumentException(START_TIME_FUTURE_MESSAGE);
        }

        if (endTime.isBefore(now)) {
            throw new IllegalArgumentException(END_TIME_FUTURE_MESSAGE);
        }

        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException(TIME_RANGE_MESSAGE);
        }
    }

    public static boolean isValidStatusTransition(FestivalProgressStatus currentStatus,
                                                  FestivalProgressStatus newStatus) {
        switch (currentStatus) {
            case UPCOMING:
                return newStatus == FestivalProgressStatus.ONGOING || newStatus == FestivalProgressStatus.COMPLETED;
            case ONGOING:
                return newStatus == FestivalProgressStatus.COMPLETED;
            case COMPLETED:
                return false; // 종료 상태에서는 다른 상태로 변경할 수 없음
            default:
                return false;
        }
    }
}