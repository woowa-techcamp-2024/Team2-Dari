package com.wootecam.festivals.domain.checkin.controller;

import com.wootecam.festivals.domain.checkin.service.CheckinService;
import com.wootecam.festivals.global.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/festivals/{festivalId}/tickets/{ticketId}/checkins")
@RequiredArgsConstructor
public class CheckinController {

    private final CheckinService checkinService;

    /**
     * 체크인 정보를 체크인 되었음으로 업데이트 합니다.
     * @param checkinId 조회할 체크인의 식별자
     * @return null
     */
    @PatchMapping("/{checkinId}")
    public ApiResponse<Void> updateCheckedIn(@PathVariable Long checkinId) {
        log.debug("체크인 업데이트 처리 요청 - 체크인 ID: {}", checkinId);
        checkinService.completeCheckin(checkinId);
        log.debug("체크인 업데이트 처리 완료 - 체크인 ID: {}", checkinId);
        return ApiResponse.empty();
    }
}
