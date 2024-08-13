package com.wootecam.festivals.domain.festival.service;

import com.wootecam.festivals.domain.festival.dto.FestivalCreateRequest;
import com.wootecam.festivals.domain.festival.dto.FestivalCreateResponse;
import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.festival.repository.FestivalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
