package com.wootecam.festivals.domain.wait.controller;

import com.wootecam.festivals.domain.wait.dto.WaitOrderCreateResponse;
import com.wootecam.festivals.domain.wait.dto.WaitOrderResponse;
import com.wootecam.festivals.domain.wait.service.WaitOrderService;
import com.wootecam.festivals.global.api.ApiResponse;
import com.wootecam.festivals.global.auth.AuthUser;
import com.wootecam.festivals.global.auth.Authentication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/festivals/{festivalId}/tickets/{ticketId}/purchase/wait")
public class WaitOrderController {

    private final WaitOrderService waitOrderService;

    /**
     * 대기열에 사용자를 추가하는 api
     *
     * @param festivalId
     * @param ticketId
     * @param authentication
     * @return 사용자의 대기열 블록 순서
     */
    @ResponseStatus(HttpStatus.OK)
    @PostMapping
    public ApiResponse<WaitOrderCreateResponse> joinQueue(@PathVariable Long festivalId,
                                                          @PathVariable Long ticketId,
                                                          @AuthUser Authentication authentication) {
        WaitOrderCreateResponse response = waitOrderService.createWaitOrder(ticketId, authentication.memberId());
        return ApiResponse.of(response);
    }

    /**
     * 대기열 통과 가능 여부 및 대기 순서 조회 API
     *
     * @param festivalId
     * @param ticketId
     * @param authentication
     * @return 대기열 통과 가능 여부, 대기 순서 응답
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public ApiResponse<WaitOrderResponse> getQueuePosition(@PathVariable Long festivalId,
                                                           @PathVariable Long ticketId,
                                                           @AuthUser Authentication authentication,
                                                           @RequestParam Long waitOrderBlock) {
        WaitOrderResponse response = waitOrderService.getWaitOrder(ticketId, authentication.memberId(), waitOrderBlock);
        return ApiResponse.of(response);
    }
}
