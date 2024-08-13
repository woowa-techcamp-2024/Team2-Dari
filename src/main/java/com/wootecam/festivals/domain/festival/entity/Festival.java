package com.wootecam.festivals.domain.festival.entity;

import com.wootecam.festivals.global.audit.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

    @NotNull
    @Column(name = "organization_id", nullable = false)
    private Long organizationId; //TODO: Organization으로 ManyToOne 변경 예정

    @NotNull
    @Column(nullable = false, length = TITLE_MAX_LENGTH)
    private String title;

    @NotNull
    @Column(nullable = false, length = DESCRIPTION_MAX_LENGTH)
    private String description;

    @NotNull
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @NotNull
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Builder
    private Festival(Long organizationId, String title, String description, LocalDateTime startTime,
                     LocalDateTime endTime) {
        this.organizationId = Objects.requireNonNull(organizationId);
        this.title = Objects.requireNonNull(title);
        this.description = Objects.requireNonNull(description);
        this.startTime = Objects.requireNonNull(startTime);
        this.endTime = Objects.requireNonNull(endTime);
        validate();
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
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("시작 시간은 종료 시간보다 앞어야만 합니다.");
        }
    }
}
