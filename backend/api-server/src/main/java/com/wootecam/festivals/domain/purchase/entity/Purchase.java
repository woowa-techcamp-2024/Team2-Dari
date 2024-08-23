package com.wootecam.festivals.domain.purchase.entity;

import com.wootecam.festivals.domain.member.entity.Member;
import com.wootecam.festivals.domain.ticket.entity.Ticket;
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
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "purchase")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Purchase extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "purchase_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false, updatable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, updatable = false)
    private Member member;

    @Column(name = "purchase_time", nullable = false)
    private LocalDateTime purchaseTime;

    @Column(name = "purchase_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private PurchaseStatus purchaseStatus;

    @Builder
    private Purchase(Ticket ticket, Member member,
                     LocalDateTime purchaseTime, PurchaseStatus purchaseStatus) {
        this.ticket = Objects.requireNonNull(ticket, "티켓 정보는 필수입니다.");
        this.member = Objects.requireNonNull(member, "회원 정보는 필수입니다.");
        this.purchaseTime = Objects.requireNonNull(purchaseTime, "구매 시간은 필수입니다.");
        this.purchaseStatus = Objects.requireNonNull(purchaseStatus, "구매 상태는 필수입니다.");
    }
}
