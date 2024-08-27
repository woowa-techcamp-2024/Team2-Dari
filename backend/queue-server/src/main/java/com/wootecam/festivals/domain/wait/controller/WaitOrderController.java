package com.wootecam.festivals.domain.wait.controller;

import com.wootecam.festivals.domain.wait.WaitOrderResponse;
import com.wootecam.festivals.domain.wait.service.WaitOrderService;
import com.wootecam.festivals.global.api.ApiResponse;
import com.wootecam.festivals.global.auth.AuthUser;
import com.wootecam.festivals.global.auth.Authentication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class WaitOrderController {

    private final WaitOrderService waitOrderService;

    /**
     * 대기열 통과 가능 여부 조회 API
     *
     * @param festivalId
     * @param ticketId
     * @param authentication
     * @return 대기열 통과 가능 여부, 대기 순서 응답
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/api/v1/festivals/{festivalId}/tickets/{ticketId}/purchase/wait")
    public ApiResponse<WaitOrderResponse> getQueuePosition(@PathVariable Long festivalId,
                                                           @PathVariable Long ticketId,
                                                           @AuthUser Authentication authentication) {
        WaitOrderResponse waitOrder = waitOrderService.getWaitOrder(ticketId, authentication.memberId());
        return ApiResponse.of(waitOrder);
    }
}