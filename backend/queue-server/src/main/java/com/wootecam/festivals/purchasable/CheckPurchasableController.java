package com.wootecam.festivals.purchasable;

import com.wootecam.festivals.global.api.ApiResponse;
import com.wootecam.festivals.global.auth.AuthErrorCode;
import com.wootecam.festivals.global.auth.AuthUser;
import com.wootecam.festivals.global.auth.Authentication;
import com.wootecam.festivals.global.exception.type.ApiException;
import com.wootecam.festivals.global.utils.SessionUtils;
import com.wootecam.festivals.global.utils.TimeProvider;
import com.wootecam.festivals.purchasable.dto.PurchasableResponse;
import com.wootecam.festivals.purchasable.dto.WaitOrderResponse;
import com.wootecam.festivals.purchasable.service.CheckPurchasableService;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
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

    public static final String PURCHASABLE_TICKET_KEY = "purchasable_ticket_id";
    public static final String PURCHASABLE_TICKET_TIMESTAMP_KEY = "purchasable_ticket_timestamp";

    private final CheckPurchasableService checkPurchasableService;
    private final TimeProvider timeProvider;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/api/v1/festivals/{festivalId}/tickets/{ticketId}/purchase/wait")
    public ApiResponse<WaitOrderResponse> getQueuePosition(@PathVariable Long festivalId,
                                                           @PathVariable Long ticketId,
                                                           @AuthUser Authentication authentication) {

        // isPurchasableOrder(): 구매 가능한 대기 순서인지 확인, // 대기열에 없는 기반으로

        // false: 200 - false, 대기열 순서 : 구매 가능한 대기 순서가 아니라면 return 200, false, 현재 대기열 순서 확인
        // 200 - true
        WaitOrderResponse waitOrder = checkPurchasableService.getWaitOrder(ticketId, authentication.memberId(),
                timeProvider.getCurrentTime());
        return ApiResponse.of(waitOrder);
    }

    // 결제 -> 외부 결제 API
    // 구매 -> 결제 + mysql 동기화

    /**
     * 티켓 결제 가능 여부 확인 API
     *
     * @param festivalId
     * @param ticketId
     * @param authentication
     * @return 티켓 결제 가능 여부 응답
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/api/v1/festivals/{festivalId}/tickets/{ticketId}/purchase/check")
    public ApiResponse<PurchasableResponse> checkPurchasable(@PathVariable Long festivalId,
                                                             @PathVariable Long ticketId,
                                                             @AuthUser Authentication authentication) {
        // waitOrder = 현재 대기 순서 조회
        // 현재 대기 순서가 PURCHASABLE_QUEUE_SIZE 초과라면
        // return 200 - false, waitOrder

        //Tx2
        // 현재 재고 > 0
        // false: 현재 재고가 남아있는지 확인하여 없으면 return 400 매진

        // validFirstPurchase(memberId, ticketId)
        // false : 첫 구매인지 확인하여 구매 내역이 있으면 return 400 이미 구매

        //Tx3
        // publishPurchasableToken(), 토큰을 세션에 설정, redis 재고 차감
        // return: 200 - true, null


        Long requestMemberId = authentication.memberId();
        log.debug("티켓 구매 가능 여부 확인 - 유저 ID: {}, 축제 ID: {}, 티켓 ID: {}", requestMemberId, festivalId, ticketId);
        PurchasableResponse purchasableResponse = checkPurchasableService.checkPurchasable(ticketId, requestMemberId,
                timeProvider.getCurrentTime());

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
