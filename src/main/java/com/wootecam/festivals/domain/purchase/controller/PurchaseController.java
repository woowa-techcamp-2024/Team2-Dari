package com.wootecam.festivals.domain.purchase.controller;

import com.wootecam.festivals.domain.purchase.dto.PurchaseTicketResponse;
import com.wootecam.festivals.domain.purchase.service.PurchaseFacadeService;
import com.wootecam.festivals.global.api.ApiResponse;
import com.wootecam.festivals.global.auth.AuthUser;
import com.wootecam.festivals.global.auth.Authentication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 티켓 구매 관련 API를 처리하는 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/festivals/{festivalId}/tickets/{ticketId}/purchase")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseFacadeService purchaseFacadeService;

    /**
     * 티켓 구매 API
     *
     * @param festivalId     축제 ID
     * @param ticketId       티켓 ID
     * @param authentication 인증 정보
     * @return 구매된 티켓 ID 응답
     */
    @ResponseStatus(HttpStatus.OK)
    @PostMapping
    public ApiResponse<PurchaseTicketResponse> createPurchase(@PathVariable Long festivalId,
                                                          @PathVariable Long ticketId,
                                                          @AuthUser Authentication authentication) {

        log.debug("티켓 구매 요청 - 축제 ID: {}, 티켓 ID: {}, 회원 ID: {}", festivalId, ticketId, authentication.memberId());
        PurchaseTicketResponse response = purchaseFacadeService.purchaseTicket(authentication.memberId(),
                festivalId, ticketId);
        log.debug("티켓 구매 완료 - 구매 ID: {}, 체크인 ID: {}", response.purchaseId(), response.checkinId());

        return ApiResponse.of(response);
    }
}