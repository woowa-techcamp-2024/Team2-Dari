package com.wootecam.festivals.domain.checkin.entity;

import com.wootecam.festivals.domain.checkin.exception.CheckinErrorCode;
import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.member.entity.Member;
import com.wootecam.festivals.domain.ticket.entity.Ticket;
import com.wootecam.festivals.global.audit.BaseEntity;
import com.wootecam.festivals.global.exception.type.ApiException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
public class Checkin extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "checkin_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "festival_id", nullable = false, updatable = false)
    @NotNull
    private Festival festival;

    @ManyToOne
    @Column(name = "member_id", nullable = false, updatable = false)
    @NotNull
    private Member member;

    @ManyToOne
    @NotNull
    @Column(name = "ticket_id", nullable = false, updatable = false)
    private Ticket ticket;

    @Column(name = "checkin_time")
    private LocalDateTime checkinTime; // 체크인하지 않은 경우 null

    @NotNull
    @Column(name = "is_checked", nullable = false)
    private boolean isChecked;

    @Builder
    private Checkin(Festival festival, Member member, Ticket ticket) {
        this.festival = Objects.requireNonNull(festival);
        this.member = Objects.requireNonNull(member);
        this.ticket = Objects.requireNonNull(ticket);
        this.checkinTime = null;
        this.isChecked = false;
    }

    public void updateCheckedIn() {
        // 이미 체크인한 경우 에러 발생
        if (isCheckedIn()) {
            throw new ApiException(CheckinErrorCode.ALREADY_CHECKED_IN);
        }

        isChecked = true;
        checkinTime = LocalDateTime.now();
    }

    public boolean isCheckedIn() {
        return isChecked && checkinTime != null;
    }
}
