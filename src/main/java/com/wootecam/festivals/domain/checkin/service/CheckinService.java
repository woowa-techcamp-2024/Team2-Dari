package com.wootecam.festivals.domain.checkin.service;

import com.wootecam.festivals.domain.checkin.entity.Checkin;
import com.wootecam.festivals.domain.checkin.exception.CheckinErrorCode;
import com.wootecam.festivals.domain.checkin.repository.CheckinRepository;
import com.wootecam.festivals.domain.member.entity.Member;
import com.wootecam.festivals.domain.member.repository.MemberRepository;
import com.wootecam.festivals.domain.ticket.entity.Ticket;
import com.wootecam.festivals.domain.ticket.repository.TicketRepository;
import com.wootecam.festivals.global.exception.type.ApiException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckinService {

    private final CheckinRepository checkinRepository;
    private final MemberRepository memberRepository;
    private final TicketRepository ticketRepository;

    @Transactional
    public Long saveCheckin(Long memberId, Long ticketId) {
        Member member = memberRepository.getReferenceById(memberId);
        Ticket ticket = ticketRepository.getReferenceById(ticketId);
        
        checkinRepository.findByMemberIdAndTicketId(memberId, ticketId)
                .ifPresent(checkin -> {
                    log.error("이미 저장된 체크인 정보가 존재합니다. memberId={}, ticketId={}", memberId, ticketId);
                    throw new ApiException(CheckinErrorCode.ALREADY_SAVED_CHECKIN);
                });

        Checkin savedCheckin = checkinRepository.save(Checkin.builder()
                .member(member)
                .ticket(ticket)
                .build());

        log.info("체크인 정보 저장: memberId={}, ticketId={}", memberId, ticketId);
        return savedCheckin.getId();
    }

    @Transactional
    public void updateCheckedIn(Long checkinId) {
        Checkin checkin = checkinRepository.findById(checkinId)
                .orElseThrow(() -> new ApiException(CheckinErrorCode.CHECKIN_NOT_FOUND));

        checkin.updateCheckedIn();

        log.info("체크인 완료 처리: checkinId={}", checkinId);
    }
}
