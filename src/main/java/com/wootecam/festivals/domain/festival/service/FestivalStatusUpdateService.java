package com.wootecam.festivals.domain.festival.service;

import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.festival.entity.FestivalProgressStatus;
import com.wootecam.festivals.domain.festival.exception.FestivalErrorCode;
import com.wootecam.festivals.domain.festival.repository.FestivalRepository;
import com.wootecam.festivals.global.exception.type.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FestivalStatusUpdateService {

    private final FestivalRepository festivalRepository;

    @Transactional
    public void updateFestivalStatus(Long festivalId, FestivalProgressStatus newStatus) {
        Festival festival = festivalRepository.findById(festivalId)
                .orElseThrow(() -> new ApiException(FestivalErrorCode.FestivalNotFoundException));

        festival.updateFestivalStatus(newStatus);
    }
}
