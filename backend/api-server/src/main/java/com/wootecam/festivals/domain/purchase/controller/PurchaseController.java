package com.wootecam.festivals.domain.purchase.controller;

import com.wootecam.festivals.domain.purchase.dto.PurchasableResponse;
import com.wootecam.festivals.domain.purchase.dto.PurchasePreviewInfoResponse;
import com.wootecam.festivals.domain.purchase.dto.PurchaseTicketResponse;
import com.wootecam.festivals.domain.purchase.service.PurchaseFacadeService;
import com.wootecam.festivals.domain.purchase.service.PurchaseService;
import com.wootecam.festivals.global.api.ApiResponse;
import com.wootecam.festivals.global.auth.AuthUser;
import com.wootecam.festivals.global.auth.Authentication;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
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

    public static final String PURCHASABLE_TICKET_STOCK_KEY = "purchasable_ticket_stock_id";
    public static final String PURCHASABLE_TICKET_TIMESTAMP_KEY = "purchasable_ticket_timestamp";

    private final PurchaseFacadeService purchaseFacadeService;
    private final PurchaseService purchaseService;

    /**
     * 티켓 구매 가능 여부 확인 API
     *
     * @param festivalId
     * @param ticketId
     * @param authentication
     * @return 티켓 구매 가능 여부 응답
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/check")
    public ApiResponse<PurchasableResponse> checkPurchasable(@PathVariable Long festivalId,
                                                             @PathVariable Long ticketId,
                                                             @AuthUser Authentication authentication) {
        Long requestMemberId = authentication.memberId();
        log.debug("티켓 구매 가능 여부 확인 - 유저 ID: {}, 축제 ID: {}, 티켓 ID: {}", requestMemberId, festivalId, ticketId);
        PurchasableResponse purchasableResponse = purchaseService.checkPurchasable(ticketId, requestMemberId,
                LocalDateTime.now());

        return ApiResponse.of(purchasableResponse);
    }

    /**
     * 티켓 구매 페이지의 구매 정보 미리보기 정보 조회 API
     *
     * @param festivalId
     * @param ticketId
     * @param authentication
     * @return 구매 미리보기 정보 응답
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{purchaseSessionId}")
    public ApiResponse<PurchasePreviewInfoResponse> getPurchasePreviewInfo(@PathVariable Long festivalId,
                                                                           @PathVariable Long ticketId,
                                                                           @PathVariable String purchaseSessionId,
                                                                           @AuthUser Authentication authentication) {
        Long requestMemberId = authentication.memberId();
        log.debug("티켓 구매 미리보기 정보 요청 - 유저 ID: {},축제 ID: {}, 티켓 ID: {}", requestMemberId, festivalId, ticketId);
        PurchasePreviewInfoResponse response = purchaseService.getPurchasePreviewInfo(requestMemberId, festivalId, ticketId, purchaseSessionId);
        log.debug("티켓 구매 미리보기 정보 응답 - 유저 ID: {}, 축제 ID: {}, 티켓 ID: {}", requestMemberId, festivalId, ticketId);

        return ApiResponse.of(response);
    }

    /**
     * 티켓 구매 API
     *
     * @param festivalId     축제 ID
     * @param ticketId       티켓 ID
     * @param authentication 인증 정보
     * @return 구매된 티켓 ID 응답
     */
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/{purchaseSessionId}")
    public ApiResponse<PurchaseTicketResponse> createPurchase(@PathVariable Long festivalId,
                                                              @PathVariable Long ticketId,
                                                              @PathVariable String purchaseSessionId,
                                                              @AuthUser Authentication authentication) {
        purchaseService.validPurchasableMember(purchaseSessionId, ticketId, authentication.memberId());

        log.debug("티켓 구매 요청 - 축제 ID: {}, 티켓 ID: {}, 회원 ID: {}", festivalId, ticketId, authentication.memberId());
        PurchaseTicketResponse response = purchaseFacadeService.purchaseTicket(authentication.memberId(),
                festivalId, ticketId, purchaseSessionId);
        log.debug("티켓 구매 완료 - 구매 ID: {}, 체크인 ID: {}", response.purchaseId(), response.checkinId());

        return ApiResponse.of(response);
    }
}
