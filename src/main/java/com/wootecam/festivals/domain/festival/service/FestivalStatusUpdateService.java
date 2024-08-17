package com.wootecam.festivals.domain.festival.service;

import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.festival.entity.FestivalProgressStatus;
import com.wootecam.festivals.domain.festival.exception.FestivalErrorCode;
import com.wootecam.festivals.domain.festival.repository.FestivalRepository;
import com.wootecam.festivals.global.exception.type.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 축제 상태 업데이트를 처리하는 서비스입니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FestivalStatusUpdateService {

    private final FestivalRepository festivalRepository;

    /**
     * 주어진 축제 ID에 해당하는 축제의 상태를 업데이트합니다.
     *
     * @param festivalId 업데이트할 축제의 ID
     * @param newStatus  새로운 축제 상태
     * @throws ApiException 축제를 찾을 수 없는 경우 발생
     */
    @Transactional
    public void updateFestivalStatus(Long festivalId, FestivalProgressStatus newStatus) {
        log.debug("축제 상태 업데이트 시도 - 축제 ID: {}, 새로운 상태: {}", festivalId, newStatus);

        Festival festival = festivalRepository.findById(festivalId)
                .orElseThrow(() -> {
                    log.error("축제를 찾을 수 없음 - 축제 ID: {}", festivalId);
                    return new ApiException(FestivalErrorCode.FESTIVAL_NOT_FOUND);
                });

        FestivalProgressStatus oldStatus = festival.getFestivalProgressStatus();
        festival.updateFestivalStatus(newStatus);

        log.info("축제 상태 업데이트 완료 - 축제 ID: {}, 이전 상태: {}, 새로운 상태: {}",
                festivalId, oldStatus, newStatus);
    }
}
