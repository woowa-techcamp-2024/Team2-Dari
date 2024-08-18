package com.wootecam.festivals.domain.festival.entity;

import com.wootecam.festivals.domain.festival.util.FestivalValidator;
import com.wootecam.festivals.domain.member.entity.Member;
import com.wootecam.festivals.global.audit.BaseEntity;
import com.wootecam.festivals.global.utils.DateTimeUtils;
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
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "festival")
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

    @Column(name = "festival_img", nullable = true)
    private String festivalImg;

    @Column(name = "festival_start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "festival_end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "festival_publication_status", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private FestivalPublicationStatus festivalPublicationStatus;

    @Column(name = "festival_progress_status", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private FestivalProgressStatus festivalProgressStatus;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @Builder
    private Festival(Member admin, String title, String description, String festivalImg, LocalDateTime startTime,
                     LocalDateTime endTime, FestivalPublicationStatus festivalPublicationStatus,
                     FestivalProgressStatus festivalProgressStatus) {
        FestivalValidator.validateFestival(admin, title, description, startTime, endTime);
        this.admin = admin;
        this.title = title;
        this.description = description;
        this.festivalImg = festivalImg;
        this.startTime = DateTimeUtils.normalizeDateTime(startTime);
        this.endTime = DateTimeUtils.normalizeDateTime(endTime);
        this.festivalPublicationStatus =
                festivalPublicationStatus == null ? FestivalPublicationStatus.PUBLISHED : festivalPublicationStatus;
        this.festivalProgressStatus =
                festivalProgressStatus == null ? FestivalProgressStatus.UPCOMING : festivalProgressStatus;
        this.isDeleted = false;
    }

    public void delete() {
        isDeleted = true;
    }

    public void updateFestivalStatus(FestivalProgressStatus newStatus) {
        if (!FestivalValidator.isValidStatusTransition(this.festivalProgressStatus, newStatus)) {
            throw new IllegalStateException("상태 변경 불가 from " + this.festivalProgressStatus + " to " + newStatus);
        }
        this.festivalProgressStatus = newStatus;
    }
}
