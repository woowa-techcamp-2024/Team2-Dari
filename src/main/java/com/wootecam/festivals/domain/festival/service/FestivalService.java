package com.wootecam.festivals.domain.festival.service;

import com.wootecam.festivals.domain.festival.dto.FestivalCreateRequestDto;
import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.festival.repository.FestivalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FestivalService {

    private final FestivalRepository festivalRepository;

    public Long createFestival(FestivalCreateRequestDto requestDto) {
        Festival festival = requestDto.toEntity();
        // TODO: festival 유효성 검사 필요
        Festival savedFestival = festivalRepository.save(festival);
        return savedFestival.getId();
    }
}
