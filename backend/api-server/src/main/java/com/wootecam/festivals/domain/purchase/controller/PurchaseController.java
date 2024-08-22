package com.wootecam.festivals.domain.purchase.controller;

import com.wootecam.festivals.domain.auth.exception.AuthErrorCode;
import com.wootecam.festivals.domain.payment.service.PaymentService;
import com.wootecam.festivals.domain.purchase.dto.PaymentIdResponse;
import com.wootecam.festivals.domain.purchase.dto.PaymentStatusResponse;
import com.wootecam.festivals.domain.purchase.dto.PurchasableResponse;
import com.wootecam.festivals.domain.purchase.dto.PurchasePreviewInfoResponse;
import com.wootecam.festivals.domain.purchase.service.PurchaseFacadeService;
import com.wootecam.festivals.domain.purchase.service.PurchaseService;
import com.wootecam.festivals.global.api.ApiResponse;
import com.wootecam.festivals.global.auth.AuthUser;
import com.wootecam.festivals.global.auth.Authentication;
import com.wootecam.festivals.global.exception.type.ApiException;
import com.wootecam.festivals.global.queue.dto.PurchaseData;
import com.wootecam.festivals.global.utils.SessionUtils;
import jakarta.servlet.http.HttpSession;
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

    public static final String PURCHASABLE_TICKET_KEY = "purchasable_ticket_id";
    public static final String PURCHASABLE_TICKET_TIMESTAMP_KEY = "purchasable_ticket_timestamp";

    private final PurchaseFacadeService purchaseFacadeService;
    private final PurchaseService purchaseService;

    /**
     * 티켓 결제 가능 여부 확인 API
     *
     * @param festivalId
     * @param ticketId
     * @param authentication
     * @return
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

        if (purchasableResponse.purchasable()) {
            HttpSession session = getHttpSession();
            session.setAttribute(PURCHASABLE_TICKET_KEY, ticketId);
            LocalDateTime purchasableTicketTimestamp = LocalDateTime.now().plusMinutes(5);
            session.setAttribute(PURCHASABLE_TICKET_TIMESTAMP_KEY, purchasableTicketTimestamp);

            log.debug("티켓 구매 가능 - 유효 시각: {}, 티켓 ID: {}", purchasableTicketTimestamp, ticketId);
        }

        return ApiResponse.of(purchasableResponse);
    }

    /**
     * 티켓 구매 창에서 결제 창으로 넘어갈 때 결제 정보 미리보기 정보 조회 API
     *
     * @param festivalId
     * @param ticketId
     * @param authentication
     * @return 구매 미리보기 정보 응답
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public ApiResponse<PurchasePreviewInfoResponse> getPurchasePreviewInfo(@PathVariable Long festivalId,
                                                                           @PathVariable Long ticketId,
                                                                           @AuthUser Authentication authentication) {
        validPurchasableMember(ticketId);

        Long requestMemberId = authentication.memberId();
        log.debug("티켓 구매 미리보기 정보 요청 - 유저 ID: {},축제 ID: {}, 티켓 ID: {}", requestMemberId, festivalId, ticketId);
        PurchasePreviewInfoResponse response = purchaseService.getPurchasePreviewInfo(requestMemberId, festivalId,
                ticketId);
        log.debug("티켓 구매 미리보기 정보 응답 - 유저 ID: {}, 축제 ID: {}, 티켓 ID: {}", requestMemberId, festivalId, ticketId);

        return ApiResponse.of(response);
    }

    /**
     * 티켓 결제 API V2
     *
     * @param festivalId     축제 ID
     * @param ticketId       티켓 ID
     * @param authentication 인증 정보
     * @return 결제된 티켓 ID 응답
     */
    @ResponseStatus(HttpStatus.OK)
    @PostMapping
    public ApiResponse<PaymentIdResponse> startPurchase(@PathVariable Long festivalId,
                                                        @PathVariable Long ticketId,
                                                        @AuthUser Authentication authentication) {
        validPurchasableMember(ticketId);

        log.debug("티켓 결제 요청 - 축제 ID: {}, 티켓 ID: {}, 회원 ID: {}", festivalId, ticketId, authentication.memberId());
        String paymentId = purchaseFacadeService.processPurchase(new PurchaseData(authentication.memberId(), ticketId));

        HttpSession session = getHttpSession();
        session.removeAttribute(PURCHASABLE_TICKET_KEY);
        session.removeAttribute(PURCHASABLE_TICKET_TIMESTAMP_KEY);

        return ApiResponse.of(new PaymentIdResponse(paymentId));
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{paymentId}/status")
    public ApiResponse<PaymentStatusResponse> getPaymentStatus(@PathVariable Long festivalId,
                                                               @PathVariable Long ticketId,
                                                               @PathVariable String paymentId,
                                                               @AuthUser Authentication authentication) {
        log.debug("Checking purchase status festivalId : {}, ticketId : {}, memberId : {}", festivalId, ticketId,
                authentication.memberId());

        PaymentService.PaymentStatus status = purchaseFacadeService.getPaymentStatus(paymentId);
        return ApiResponse.of(new PaymentStatusResponse(status));
    }

    private void validPurchasableMember(Long ticketId) {
        if (getHttpSession().getAttribute(PURCHASABLE_TICKET_KEY) == null
                || !ticketId.equals(getHttpSession().getAttribute(PURCHASABLE_TICKET_KEY))) {

            throw new ApiException(AuthErrorCode.FORBIDDEN);
        }
    }

    /**
     * 현재 존재하는 세션을 가져옵니다. 세션이 없다면 UnAuthorized 예외를 발생시킵니다.
     *
     * @return
     */
    private HttpSession getHttpSession() {
        HttpSession existSession = SessionUtils.getExistSession();
        if (existSession == null) {
            throw new ApiException(AuthErrorCode.UNAUTHORIZED);
        }

        return existSession;
    }
}
