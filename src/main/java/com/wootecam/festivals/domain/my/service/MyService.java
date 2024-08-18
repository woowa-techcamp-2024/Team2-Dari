package com.wootecam.festivals.domain.my.service;

import com.wootecam.festivals.domain.festival.repository.FestivalRepository;
import com.wootecam.festivals.domain.my.dto.MyFestivalCursor;
import com.wootecam.festivals.domain.my.dto.MyFestivalResponse;
import com.wootecam.festivals.global.page.CursorBasedPage;
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

    public static final int OWNED_FESTIVAL_PAGE_SIZE = 10;

    private final FestivalRepository festivalRepository;

    /**
     * 사용자가 개최한 축제 목록을 조회합니다.
     *
     * @param loginMemberId 조회할 사용자 ID
     * @param cursor        이전 페이지의 마지막 축제의 정보
     * @return 사용자가 개최한 축제 목록
     */
    public CursorBasedPage<MyFestivalResponse, MyFestivalCursor> findHostedFestival(Long loginMemberId,
                                                                                    MyFestivalCursor cursor) {
        List<MyFestivalResponse> festivalDtos = findMyFestivalNextPage(loginMemberId, cursor);
        return new CursorBasedPage<>(festivalDtos, createCursor(festivalDtos), OWNED_FESTIVAL_PAGE_SIZE);
    }

    private List<MyFestivalResponse> findMyFestivalNextPage(Long loginMemberId, MyFestivalCursor cursor) {
        if (cursor == null || cursor.id() == null || cursor.startTime() == null) {
            return festivalRepository.findFestivalsByAdminOrderStartTimeDesc(loginMemberId, Pageable.ofSize(
                    OWNED_FESTIVAL_PAGE_SIZE + 1));
        }
        return festivalRepository.findFestivalsByAdminAndCursorOrderStartTimeDesc(loginMemberId, cursor.startTime(),
                cursor.id(), Pageable.ofSize(OWNED_FESTIVAL_PAGE_SIZE + 1));
    }

    private MyFestivalCursor createCursor(List<MyFestivalResponse> festivalDtos) {
        if (festivalDtos.isEmpty()) {
            return null;
        }
        MyFestivalResponse lastFestival = festivalDtos.get(Math.min(festivalDtos.size(), OWNED_FESTIVAL_PAGE_SIZE) - 1);

        return new MyFestivalCursor(lastFestival.startTime(), lastFestival.festivalId());
    }
}