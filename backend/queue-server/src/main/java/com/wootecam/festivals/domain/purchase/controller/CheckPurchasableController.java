package com.wootecam.festivals.domain.purchase.controller;

import com.wootecam.festivals.domain.purchase.PurchasableResponse;
import com.wootecam.festivals.domain.purchase.service.CheckPurchasableService;
import com.wootecam.festivals.global.api.ApiResponse;
import com.wootecam.festivals.global.auth.AuthUser;
import com.wootecam.festivals.global.auth.Authentication;
import com.wootecam.festivals.global.utils.TimeProvider;
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
public class CheckPurchasableController {

    private final CheckPurchasableService checkPurchasableService;
    private final TimeProvider timeProvider;

    /**
     * 티켓 구매 권한 부여 API
     *
     * @param festivalId
     * @param ticketId
     * @param authentication
     * @return 티켓 구매 가능 여부 응답
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/api/v1/festivals/{festivalId}/tickets/{ticketId}/purchase/check")
    public ApiResponse<PurchasableResponse> checkPurchasable(@PathVariable Long festivalId,
                                                             @PathVariable Long ticketId,
                                                             @AuthUser Authentication authentication) {
        Long requestMemberId = authentication.memberId();
        log.debug("티켓 구매 가능 여부 확인 - 유저 ID: {}, 축제 ID: {}, 티켓 ID: {}", requestMemberId, festivalId, ticketId);
        PurchasableResponse purchasableResponse = checkPurchasableService.checkPurchasable(ticketId, requestMemberId,
                timeProvider.getCurrentTime());

        return ApiResponse.of(purchasableResponse);
    }
}
