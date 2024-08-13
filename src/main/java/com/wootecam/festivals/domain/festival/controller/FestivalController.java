package com.wootecam.festivals.domain.festival.controller;

import com.wootecam.festivals.domain.festival.dto.FestivalCreateRequestDto;
import com.wootecam.festivals.domain.festival.dto.FestivalCreateResponseDto;
import com.wootecam.festivals.domain.festival.service.FestivalService;
import com.wootecam.festivals.global.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/festivals")
@RequiredArgsConstructor
public class FestivalController {

    private final FestivalService festivalService;

    @PostMapping
    public ApiResponse<FestivalCreateResponseDto> createFestival(
            @Valid @RequestBody FestivalCreateRequestDto requestDto) {
        FestivalCreateResponseDto responseDto = festivalService.createFestival(requestDto);
        return ApiResponse.of(responseDto);
    }
}