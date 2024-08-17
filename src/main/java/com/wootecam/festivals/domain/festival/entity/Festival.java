package com.wootecam.festivals.domain.festival.entity;

import com.wootecam.festivals.domain.member.entity.Member;
import com.wootecam.festivals.global.audit.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Festival extends BaseEntity {

    private static final int TITLE_MAX_LENGTH = 100;
    private static final int DESCRIPTION_MAX_LENGTH = 2000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "festival_id")
    private Long id;

    @JoinColumn(name = "admin_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Member admin;

    @Column(name = "festival_title", nullable = false, length = TITLE_MAX_LENGTH)
    private String title;

    @Column(name = "festival_description", nullable = false, length = DESCRIPTION_MAX_LENGTH)
    private String description;

    @NotNull
    @Column(name = "festival_start_time", nullable = false)
    private LocalDateTime startTime;

    @NotNull
    @Column(name = "festival_end_time", nullable = false)
    private LocalDateTime endTime;

    @NotNull
    @Enumerated(value = EnumType.STRING)
    private FestivalPublicationStatus festivalPublicationStatus;

    @NotNull
    @Enumerated(value = EnumType.STRING)
    private FestivalProgressStatus festivalProgressStatus;

    @NotNull
    private boolean isDeleted;

    @Builder
    private Festival(Member admin, String title, String description, LocalDateTime startTime,
                     LocalDateTime endTime, FestivalPublicationStatus festivalPublicationStatus,
                     FestivalProgressStatus festivalProgressStatus) {
        this.admin = Objects.requireNonNull(admin);
        this.title = Objects.requireNonNull(title);
        this.description = Objects.requireNonNull(description);
        this.startTime = Objects.requireNonNull(startTime);
        this.endTime = Objects.requireNonNull(endTime);
        this.festivalPublicationStatus = festivalPublicationStatus
                == null ? FestivalPublicationStatus.DRAFT : festivalPublicationStatus;
        this.festivalProgressStatus =
                festivalProgressStatus == null ? FestivalProgressStatus.UPCOMING : festivalProgressStatus;
        this.isDeleted = false;
        validate();
    }

    public void delete() {
        isDeleted = true;
    }

    private void validate() {
        validateTitle();
        validateDescription();
        validateTimeRange();
    }

    private void validateTitle() {
        if (title.isEmpty()) {
            throw new IllegalArgumentException("제목은 비어있을 수 없습니다.");
        }
        if (title.length() > TITLE_MAX_LENGTH) {
            throw new IllegalArgumentException("제목의 길이는 " + TITLE_MAX_LENGTH + "를 초과해서는 안됩니다.");
        }
    }

    private void validateDescription() {
        if (description.isEmpty()) {
            throw new IllegalArgumentException("Description은 비어있을 수 없습니다.");
        }
        if (description.length() > DESCRIPTION_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Description의 길이는 " + DESCRIPTION_MAX_LENGTH + "를 초과해서는 안됩니다.");
        }
    }

    private void validateTimeRange() {
        LocalDateTime now = LocalDateTime.now().minusMinutes(1); // 1분의 여유를 둡니다.

        if (startTime.isBefore(now)) {
            throw new IllegalArgumentException("시작 시간은 현재보다 미래여야 합니다.");
        }

        if (endTime.isBefore(now)) {
            throw new IllegalArgumentException("종료 시간은 현재보다 미래여야 합니다.");
        }

        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("시작 시간은 종료 시간보다 앞어야만 합니다.");
        }
    }

    public void updateFestivalStatus(FestivalProgressStatus newStatus) {
        if (!isValidStatusTransition(this.festivalProgressStatus, newStatus)) {
            throw new IllegalStateException(
                    "상태 변경 불가 from " + this.festivalProgressStatus + " to " + newStatus);
        }
        this.festivalProgressStatus = newStatus;
    }

    private boolean isValidStatusTransition(FestivalProgressStatus currentStatus, FestivalProgressStatus newStatus) {
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
