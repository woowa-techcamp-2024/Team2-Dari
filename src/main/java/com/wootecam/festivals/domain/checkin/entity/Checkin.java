package com.wootecam.festivals.domain.checkin.entity;

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
public class Checkin extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "checkin_id")
    private Long id;

    @NotNull
    @Column(name = "festival_id", nullable = false)
    private Long festivalId;

    @NotNull
    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @NotNull
    @Column(name = "ticket_id", nullable = false)
    private Long ticketId;

    @Column(name = "checkin_time")
    private LocalDateTime checkinTime; // 체크인하지 않은 경우 null

    @NotNull
    @Column(name = "is_checked", nullable = false)
    private boolean isChecked;

    @Builder
    private Checkin(Long festivalId, Long memberId, Long ticketId) {
        this.festivalId = Objects.requireNonNull(festivalId);
        this.memberId = Objects.requireNonNull(memberId);
        this.ticketId = Objects.requireNonNull(ticketId);
        this.checkinTime = null;
        this.isChecked = false;
    }

    public void updateCheckedIn() {
        isChecked = true;
        checkinTime = LocalDateTime.now();
    }
}
