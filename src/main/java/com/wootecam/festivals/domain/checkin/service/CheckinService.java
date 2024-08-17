package com.wootecam.festivals.domain.checkin.service;

import com.wootecam.festivals.domain.checkin.entity.Checkin;
import com.wootecam.festivals.domain.checkin.exception.CheckinErrorCode;
import com.wootecam.festivals.domain.checkin.repository.CheckinRepository;
import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.festival.repository.FestivalRepository;
import com.wootecam.festivals.domain.member.entity.Member;
import com.wootecam.festivals.domain.member.repository.MemberRepository;
import com.wootecam.festivals.domain.ticket.entity.Ticket;
import com.wootecam.festivals.domain.ticket.repository.TicketRepository;
import com.wootecam.festivals.global.exception.type.ApiException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CheckinService {

    private final CheckinRepository checkinRepository;
    private final MemberRepository memberRepository;
    private final FestivalRepository festivalRepository;
    private final TicketRepository ticketRepository;

    @Transactional
    public Long saveCheckin(Long memberId, Long festivalId, Long ticketId) {
        Member member = memberRepository.getReferenceById(memberId);
        Festival festival = festivalRepository.getReferenceById(festivalId);
        Ticket ticket = ticketRepository.getReferenceById(ticketId);

        return checkinRepository.save(Checkin.builder()
                .member(member)
                .festival(festival)
                .ticket(ticket)
                .build())
                .getId();
    }

    @Transactional
    public void updateCheckedIn(Long memberId, Long festivalId, Long ticketId) {
        Checkin checkin = checkinRepository.findByMemberIdAndFestivalIdAndTicketId(memberId, festivalId, ticketId)
                .orElseThrow(() -> new ApiException(CheckinErrorCode.CHECKIN_NOT_FOUND));

        checkin.updateCheckedIn();
    }
}
