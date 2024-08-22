package com.wootecam.festivals.domain.my.service;

import com.wootecam.festivals.domain.festival.repository.FestivalRepository;
import com.wootecam.festivals.domain.my.dto.MyFestivalCursor;
import com.wootecam.festivals.domain.my.dto.MyFestivalResponse;
import com.wootecam.festivals.domain.my.dto.MyPurchasedFestivalResponse;
import com.wootecam.festivals.domain.my.dto.MyPurchasedTicketResponse;
import com.wootecam.festivals.domain.purchase.exception.PurchaseErrorCode;
import com.wootecam.festivals.domain.purchase.repository.PurchaseRepository;
import com.wootecam.festivals.global.exception.type.ApiException;
import com.wootecam.festivals.global.page.CursorBasedPage;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * 사용자가 개최하거나 참여한 축체 목록을 조회하는 서비스 클래스입니다.
 */
@Service
@RequiredArgsConstructor
public class MyService {

    private final FestivalRepository festivalRepository;
    private final PurchaseRepository purchaseRepository;

    /**
     * 사용자가 개최한 축제 목록을 조회합니다.
     *
     * @param loginMemberId 조회할 사용자 ID
     * @param cursor        이전 페이지의 마지막 축제의 정보
     * @param pageSize
     * @return 사용자가 개최한 축제 목록
     */
    public CursorBasedPage<MyFestivalResponse, MyFestivalCursor> findHostedFestival(Long loginMemberId,
                                                                                    MyFestivalCursor cursor,
                                                                                    int pageSize) {
        List<MyFestivalResponse> festivalDtos = findMyFestivalNextPage(loginMemberId, cursor, pageSize);
        return new CursorBasedPage<>(festivalDtos, createCursor(festivalDtos, pageSize), pageSize);
    }

    public MyPurchasedTicketResponse findMyPurchasedTicket(Long loginMemberId, Long ticketId) {
        return purchaseRepository.findByMemberIdAndTicketId(loginMemberId, ticketId)
                .orElseThrow(() -> new ApiException(PurchaseErrorCode.PURCHASE_NOT_FOUND));
    }

    public CursorBasedPage<MyPurchasedFestivalResponse, MyFestivalCursor> findMyPurchasedFestivals(Long loginMemberId, MyFestivalCursor cursor, int pageSize) {
        LocalDateTime cursorTime = cursor.startTime() == null ? LocalDateTime.of(3000, 12, 31, 0, 0) : cursor.startTime();
        Long cursorId = cursor.id() == null ? Long.MAX_VALUE : cursor.id();

        List<MyPurchasedFestivalResponse> festivalDtos = purchaseRepository.findPurchasedFestivalsCursorOrderPurchaseTimeDesc(
                loginMemberId,
                cursorTime, // LocalDateTime.max() 를 넣으면 에러 발생
                cursorId,
                Pageable.ofSize(pageSize + 1));

        return new CursorBasedPage<>(festivalDtos, createFestivalCursor(festivalDtos, pageSize), pageSize);
    }

    private List<MyFestivalResponse> findMyFestivalNextPage(Long loginMemberId, MyFestivalCursor cursor, int pageSize) {
        if (cursor == null || cursor.id() == null || cursor.startTime() == null) {
            return festivalRepository.findFestivalsByAdminOrderStartTimeDesc(loginMemberId, Pageable.ofSize(
                    pageSize + 1));
        }
        return festivalRepository.findFestivalsByAdminAndCursorOrderStartTimeDesc(loginMemberId, cursor.startTime(),
                cursor.id(), Pageable.ofSize(pageSize + 1));
    }

    private MyFestivalCursor createCursor(List<MyFestivalResponse> festivalDtos, int pageSize) {
        if (festivalDtos.isEmpty()) {
            return null;
        }
        MyFestivalResponse lastFestival = festivalDtos.get(Math.min(festivalDtos.size(), pageSize) - 1);

        return new MyFestivalCursor(lastFestival.startTime(), lastFestival.festivalId());
    }

    private MyFestivalCursor createFestivalCursor(List<MyPurchasedFestivalResponse> festivalDtos, int pageSize) {
        if (festivalDtos.isEmpty()) {
            return null;
        }

        MyPurchasedFestivalResponse lastFestival = festivalDtos.get(Math.min(festivalDtos.size(), pageSize) - 1);

        return new MyFestivalCursor(lastFestival.purchaseTime(), lastFestival.purchaseId());
    }
}
