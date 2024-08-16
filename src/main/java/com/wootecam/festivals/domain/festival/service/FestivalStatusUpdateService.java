package com.wootecam.festivals.domain.festival.service;

import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.festival.entity.FestivalStatus;
import com.wootecam.festivals.domain.festival.repository.FestivalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FestivalStatusUpdateService {

    private final FestivalRepository festivalRepository;

    @Transactional
    public void updateFestivalStatus(Long festivalId, FestivalStatus newStatus) {
        Festival festival = festivalRepository.findById(festivalId).orElseThrow();
        festival.updateFestivalStatus(newStatus);
    }
}
