package com.wootecam.festivals.domain.festival.service;

import com.wootecam.festivals.domain.festival.dto.Cursor;
import com.wootecam.festivals.domain.festival.dto.FestivalCreateRequest;
import com.wootecam.festivals.domain.festival.dto.FestivalCreateResponse;
import com.wootecam.festivals.domain.festival.dto.FestivalDetailResponse;
import com.wootecam.festivals.domain.festival.dto.FestivalListResponse;
import com.wootecam.festivals.domain.festival.dto.KeySetPageResponse;
import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.festival.exception.FestivalErrorCode;
import com.wootecam.festivals.domain.festival.repository.FestivalRepository;
import com.wootecam.festivals.domain.festival.util.FestivalFactory;
import com.wootecam.festivals.global.exception.type.ApiException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Slf4j
@Service
@RequiredArgsConstructor
public class FestivalService {

    private final FestivalRepository festivalRepository;
    private final FestivalFactory festivalFactory;
    private final FestivalStatusUpdateService festivalStatusUpdateService;

    @Transactional
    public FestivalCreateResponse createFestival(FestivalCreateRequest requestDto) {
        Festival festival = festivalFactory.createFromDto(requestDto);
        Festival savedFestival = festivalRepository.save(festival);

        festivalStatusUpdateService.updateFestivalStatus(savedFestival.getId(),
                savedFestival.getFestivalProgressStatus());

        return FestivalCreateResponse.from(savedFestival);
    }

    @Transactional(readOnly = true)
    public FestivalDetailResponse getFestivalDetail(Long festivalId) {
        Assert.notNull(festivalId, "Festival ID는 null일 수 없습니다.");

        log.debug("Festival 상세 정보 조회 - ID: {}", festivalId);

        Festival festival = festivalRepository.findById(festivalId)
                .orElseThrow(() -> {
                    log.warn("Festival을 찾을 수 없습니다 - ID: {}", festivalId);
                    return new ApiException(FestivalErrorCode.FESTIVAL_NOT_FOUND,
                            "Festival을 찾을 수 없습니다 - ID: " + festivalId); // + 연산의 경우 StringBuilder로 최적화된다.
                });

        log.debug("Festival 조회 완료: {}", festival);
        return FestivalDetailResponse.from(festival);
    }

    @Transactional(readOnly = true)
    public KeySetPageResponse<FestivalListResponse> getFestivals(LocalDateTime cursorTime, Long cursorId,
                                                                 int pageSize) {
        LocalDateTime now = LocalDateTime.now();
        // Pageable을 사용하여 결과의 개수를 제한합니다.
        // 이는 setMaxResults()를 사용하는 것과 같은 효과를 내지만,
        // 데이터베이스에 독립적이고 JPA의 추상화 수준을 유지합니다.
        // pageSize + 1을 요청하여 다음 페이지의 존재 여부를 확인합니다.
        Pageable pageRequest = PageRequest.of(0, pageSize + 1);

        List<FestivalListResponse> festivals = festivalRepository.findUpcomingFestivalsBeforeCursor(
                cursorTime != null ? cursorTime : now,
                cursorId != null ? cursorId : Long.MAX_VALUE,
                now,
                pageRequest);

        boolean hasNext = festivals.size() > pageSize;
        List<FestivalListResponse> pageContent = hasNext ? festivals.subList(0, pageSize) : festivals;

        LocalDateTime nextCursorTime = null;
        Long nextCursorId = null;
        if (hasNext) {
            FestivalListResponse lastFestival = pageContent.get(pageContent.size() - 1);
            nextCursorTime = lastFestival.startTime();
            nextCursorId = lastFestival.festivalId();
        }

        return new KeySetPageResponse<>(pageContent, new Cursor(nextCursorTime, nextCursorId), hasNext);
    }
}
