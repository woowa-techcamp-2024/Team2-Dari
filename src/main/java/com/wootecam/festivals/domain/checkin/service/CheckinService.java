package com.wootecam.festivals.domain.checkin.service;

import com.wootecam.festivals.domain.checkin.entity.Checkin;
import com.wootecam.festivals.domain.checkin.exception.CheckinErrorCode;
import com.wootecam.festivals.domain.checkin.repository.CheckinRepository;
import com.wootecam.festivals.global.exception.type.ApiException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CheckinService {

    private final CheckinRepository checkinRepository;

    @Transactional
    public void updateCheckedIn(Long memberId, Long festivalId, Long ticketId) {
        Checkin checkin = checkinRepository.findByMemberIdAndFestivalIdAndTicketId(memberId, festivalId, ticketId)
                .orElseThrow(() -> new ApiException(CheckinErrorCode.CHECKIN_NOT_FOUND));

        checkin.updateCheckedIn();
    }
}
