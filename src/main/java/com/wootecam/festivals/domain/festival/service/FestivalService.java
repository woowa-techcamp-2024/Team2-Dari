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
                savedFestival.getFestivalPublicationStatus());

        return FestivalCreateResponse.from(savedFestival);
    }

    @Transactional(readOnly = true)
    public FestivalDetailResponse getFestivalDetail(Long festivalId) {
        Assert.notNull(festivalId, "Festival ID는 null일 수 없습니다.");

        log.debug("Festival 상세 정보 조회 - ID: {}", festivalId);

        Festival festival = festivalRepository.findById(festivalId)
                .orElseThrow(() -> {
                    log.warn("Festival을 찾을 수 없습니다 - ID: {}", festivalId);
                    return new ApiException(FestivalErrorCode.FestivalNotFoundException,
                            "Festival을 찾을 수 없습니다 - ID: " + festivalId); // + 연산의 경우 StringBuilder로 최적화된다.
                });

        log.debug("Festival 조회 완료: {}", festival);
        return FestivalDetailResponse.from(festival);
    }

    @Transactional(readOnly = true)
    public KeySetPageResponse<FestivalListResponse> getFestivals(LocalDateTime cursorTime, Long cursorId,
                                                                 int pageSize) {
        PageRequest pageRequest = PageRequest.of(0, pageSize + 1);
        LocalDateTime now = LocalDateTime.now();

        List<Festival> festivals = festivalRepository.findUpcomingFestivalsBeforeCursor(
                cursorTime != null ? cursorTime : now,
                cursorId != null ? cursorId : Long.MAX_VALUE,
                now,
                pageRequest);

        boolean hasNext = festivals.size() > pageSize;
        List<Festival> pageContent = hasNext ? festivals.subList(0, pageSize) : festivals;

        List<FestivalListResponse> responses = pageContent.stream()
                .map(FestivalListResponse::from)
                .toList();

        LocalDateTime nextCursorTime = null;
        Long nextCursorId = null;
        if (hasNext) {
            Festival lastFestival = pageContent.get(pageContent.size() - 1);
            nextCursorTime = lastFestival.getStartTime();
            nextCursorId = lastFestival.getId();
        }

        return new KeySetPageResponse<>(responses, new Cursor(nextCursorTime, nextCursorId), hasNext);
    }
}
