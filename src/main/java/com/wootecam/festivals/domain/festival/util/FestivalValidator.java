package com.wootecam.festivals.domain.festival.util;

import com.wootecam.festivals.domain.festival.entity.FestivalProgressStatus;
import com.wootecam.festivals.domain.member.entity.Member;
import java.time.LocalDateTime;
import java.util.Objects;

public class FestivalValidator {

    private static final int TITLE_MAX_LENGTH = 100;
    private static final int DESCRIPTION_MAX_LENGTH = 2000;

    private FestivalValidator() {
    }

    public static void validateFestival(Member admin, String title, String description,
                                        LocalDateTime startTime, LocalDateTime endTime) {
        Objects.requireNonNull(admin, "관리자는 null일 수 없습니다.");
        validateTitle(title);
        validateDescription(description);
        validateTimeRange(startTime, endTime);
    }

    public static void validateTitle(String title) {
        Objects.requireNonNull(title, "제목은 null일 수 없습니다.");
        if (title.isEmpty()) {
            throw new IllegalArgumentException("제목은 비어있을 수 없습니다.");
        }
        if (title.length() > TITLE_MAX_LENGTH) {
            throw new IllegalArgumentException("제목의 길이는 " + TITLE_MAX_LENGTH + "를 초과해서는 안됩니다.");
        }
    }

    public static void validateDescription(String description) {
        Objects.requireNonNull(description, "설명은 null일 수 없습니다.");
        if (description.isEmpty()) {
            throw new IllegalArgumentException("설명은 비어있을 수 없습니다.");
        }
        if (description.length() > DESCRIPTION_MAX_LENGTH) {
            throw new IllegalArgumentException("설명의 길이는 " + DESCRIPTION_MAX_LENGTH + "를 초과해서는 안됩니다.");
        }
    }

    public static void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        Objects.requireNonNull(startTime, "시작 시간은 null일 수 없습니다.");
        Objects.requireNonNull(endTime, "종료 시간은 null일 수 없습니다.");

        LocalDateTime now = LocalDateTime.now().minusMinutes(1); // 1분의 여유를 둡니다.

        if (startTime.isBefore(now)) {
            throw new IllegalArgumentException("시작 시간은 현재보다 미래여야 합니다.");
        }

        if (endTime.isBefore(now)) {
            throw new IllegalArgumentException("종료 시간은 현재보다 미래여야 합니다.");
        }

        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("시작 시간은 종료 시간보다 앞서야 합니다.");
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