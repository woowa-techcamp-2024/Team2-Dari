package com.wootecam.festivals.domain.my.controller;


import com.wootecam.festivals.domain.my.dto.MyFestivalCursor;
import com.wootecam.festivals.domain.my.dto.MyFestivalRequestParams;
import com.wootecam.festivals.domain.my.dto.MyFestivalResponse;
import com.wootecam.festivals.domain.my.dto.MyPurchasedTicketResponse;
import com.wootecam.festivals.domain.my.service.MyService;
import com.wootecam.festivals.global.api.ApiResponse;
import com.wootecam.festivals.global.auth.AuthUser;
import com.wootecam.festivals.global.auth.Authentication;
import com.wootecam.festivals.global.page.CursorBasedPage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
public class MyController {

    private final MyService myService;

    /**
     * 사용자가 개최한 축제 목록을 조회합니다.
     *
     * @param loginMemberId 조회할 사용자 ID
     * @param cursorTime    이전 페이지의 마지막 축제의 시간 (yyyy-MM-dd'T'HH:mm 형식)
     * @param cursorId      이전 페이지의 마지막 축제의 ID
     * @return 사용자가 개최한 축제 목록
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/festivals")
    public ApiResponse<CursorBasedPage<MyFestivalResponse, MyFestivalCursor>> findHostedFestival(
            @AuthUser Authentication authentication,
            @Valid @ModelAttribute MyFestivalRequestParams params) {
        log.debug("내가 개최한 축제 목록 조회 요청 - time: {}, id: {}", params.time(), params.id());
        CursorBasedPage<MyFestivalResponse, MyFestivalCursor> myFestivalPage = myService.findHostedFestival(
                authentication.memberId(), new MyFestivalCursor(params.time(), params.id()), params.pageSize());
        log.debug("내가 개최한 축제 목록 조회 완료 - 조회된 축제 수: {}", myFestivalPage.getContent().size());

        return ApiResponse.of(myFestivalPage);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/tickets/{ticketId}")
    public ApiResponse<MyPurchasedTicketResponse> findMyPurchasedTicket(
            @AuthUser Authentication authentication,
            @PathVariable Long ticketId) {
        log.debug("내가 구매한 티켓 조회 요청 - ticketId: {}", ticketId);
        MyPurchasedTicketResponse myPurchasedTicketResponse = myService.findMyPurchasedTicket(authentication.memberId(), ticketId);
        log.debug("내가 구매한 티켓 조회 완료 - ticketId: {}", ticketId);

        return ApiResponse.of(myPurchasedTicketResponse);
    }
}
