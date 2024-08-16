package com.wootecam.festivals.domain.purchase.entity;

import com.wootecam.festivals.domain.member.entity.Member;
import com.wootecam.festivals.domain.ticket.entity.Ticket;
import com.wootecam.festivals.global.audit.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Purchase extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "purchase_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ticket_id", nullable = false, updatable = false)
    private Ticket ticket;

    @ManyToOne
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
        this.ticket = Objects.requireNonNull(ticket);
        this.member = Objects.requireNonNull(member);
        this.purchaseTime = Objects.requireNonNull(purchaseTime);
        this.purchaseStatus = Objects.requireNonNull(purchaseStatus);
    }
}
