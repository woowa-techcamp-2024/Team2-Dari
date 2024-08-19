package com.wootecam.festivals.domain.ticket.entity;

import com.wootecam.festivals.global.audit.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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
    @Column(name = "ticket_purchase_id")
    private Long id;

    @Column(name = "ticket_stock", nullable = false)
    private int remainStock;

    @OneToOne
    @JoinColumn(name = "ticket_id", nullable = false, updatable = false)
    private Ticket ticket;

    @Builder
    private TicketStock(int remainStock, Ticket ticket) {
        this.remainStock = remainStock;
        this.ticket = Objects.requireNonNull(ticket, "티켓 정보는 필수입니다.");
    }

    public void decreaseStock() {
        if (remainStock <= 0) {
            throw new IllegalStateException("재고가 없습니다.");
        }
        --remainStock;
    }
}
