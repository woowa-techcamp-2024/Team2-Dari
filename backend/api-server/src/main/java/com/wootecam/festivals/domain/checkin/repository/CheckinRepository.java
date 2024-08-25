package com.wootecam.festivals.domain.checkin.repository;

import com.wootecam.festivals.domain.checkin.entity.Checkin;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CheckinRepository extends JpaRepository<Checkin, Long> {

    @Query("""
            SELECT c.id FROM Checkin c 
            WHERE c.member.id = :memberId AND c.ticket.id = :ticketId
            """)
    Optional<Long> findByMemberIdAndTicketId(Long memberId, Long ticketId);
}
