package com.wootecam.festivals.domain.ticket.entity;

import com.wootecam.festivals.global.audit.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ticket_stock")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class TicketStock extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_stock_id")
    private Long id;

    @Column(name = "ticket_stock_member_id")
    private Long memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false, updatable = false)
    private Ticket ticket;

    @Builder
    private TicketStock(Ticket ticket) {
        this.ticket = Objects.requireNonNull(ticket, "티켓 정보는 필수입니다.");
    }

    public boolean isReserved() {
        return this.memberId != null;
    }

    public void reserveTicket(Long buyerId) {
        if (this.memberId != null) {
            throw new IllegalStateException("이미 판매된 티켓입니다.");
        }
        this.memberId = buyerId;
    }

    public void cancelTicket() {
        this.memberId = null;
    }
}
