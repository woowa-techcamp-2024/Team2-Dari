package com.wootecam.festivals.domain.checkin.repository;

import com.wootecam.festivals.domain.checkin.entity.Checkin;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckinRepository extends JpaRepository<Checkin, Long> {

    Optional<Checkin> findByMemberIdAndFestivalIdAndTicketId(Long memberId, Long festivalId, Long ticketId);
}
