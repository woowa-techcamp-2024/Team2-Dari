package com.wootecam.festivals.domain.festival.service;

import com.wootecam.festivals.domain.festival.dto.FestivalCreateRequest;
import com.wootecam.festivals.domain.festival.dto.FestivalCreateResponse;
import com.wootecam.festivals.domain.festival.dto.FestivalDetailResponse;
import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.festival.exception.FestivalErrorCode;
import com.wootecam.festivals.domain.festival.repository.FestivalRepository;
import com.wootecam.festivals.global.exception.type.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Slf4j
@Service
@RequiredArgsConstructor
public class FestivalService {

    private final FestivalRepository festivalRepository;

    public FestivalCreateResponse createFestival(FestivalCreateRequest requestDto) {
        Festival festival = requestDto.toEntity();
        // TODO: festival 유효성 검사 필요 ex) organization의 유효성 여부 등
        Festival savedFestival = festivalRepository.save(festival);
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
}
