package com.wootecam.festivals.domain.purchase.repository;

import com.wootecam.festivals.domain.member.entity.Member;
import com.wootecam.festivals.domain.purchase.entity.Purchase;
import com.wootecam.festivals.domain.ticket.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    boolean existsByTicketAndMember(Ticket ticket, Member member);
}