package com.wootecam.festivals.domain.ticket.entity;

import static com.wootecam.festivals.domain.ticket.utils.TickerValidator.validTicket;

import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.global.audit.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Ticket extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "festival_id", nullable = false, updatable = false)
    private Festival festival;

    @Column(name = "ticket_name", nullable = false)
    private String name;

    @Column(name = "ticket_detail", nullable = false)
    private String detail;

    @Column(name = "ticket_price", nullable = false)
    private Long price;

    @Column(name = "ticket_quantity", nullable = false)
    private int quantity;

    @Column(name = "start_sale_time", nullable = false)
    private LocalDateTime startSaleTime;

    @Column(name = "end_sale_time", nullable = false)
    private LocalDateTime endSaleTime;

    @Column(name = "end_refund_time", nullable = false)
    private LocalDateTime refundEndTime;

    private boolean isDeleted;

    @Builder
    public Ticket(Festival festival,
                  String name, String detail, Long price, int quantity,
                  LocalDateTime startSaleTime, LocalDateTime endSaleTime, LocalDateTime refundEndTime) {
        validTicket(festival, name, detail, price, quantity, startSaleTime, endSaleTime, refundEndTime);
        this.festival = festival;
        this.name = name;
        this.detail = detail;
        this.price = price;
        this.quantity = quantity;
        this.startSaleTime = startSaleTime;
        this.endSaleTime = endSaleTime;
        this.refundEndTime = refundEndTime;
        this.isDeleted = false;
    }
}
