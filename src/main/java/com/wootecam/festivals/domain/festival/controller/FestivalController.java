package com.wootecam.festivals.domain.festival.controller;

import com.wootecam.festivals.domain.festival.dto.FestivalCreateRequest;
import com.wootecam.festivals.domain.festival.dto.FestivalCreateResponse;
import com.wootecam.festivals.domain.festival.dto.FestivalDetailResponse;
import com.wootecam.festivals.domain.festival.service.FestivalService;
import com.wootecam.festivals.global.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/festivals")
@RequiredArgsConstructor
public class FestivalController {

    private final FestivalService festivalService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ApiResponse<FestivalCreateResponse> createFestival(
            @Valid @RequestBody FestivalCreateRequest requestDto) {
        FestivalCreateResponse responseDto = festivalService.createFestival(requestDto);
        return ApiResponse.of(responseDto);
    }

    @GetMapping("/{festivalId}")
    public ApiResponse<FestivalDetailResponse> getFestival(@PathVariable Long festivalId) {
        FestivalDetailResponse responseDto = festivalService.getFestivalDetail(festivalId);
        return ApiResponse.of(responseDto);
    }
}
