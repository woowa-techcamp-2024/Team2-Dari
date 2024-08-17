package com.wootecam.festivals.domain.festival.controller;

import com.wootecam.festivals.domain.festival.dto.FestivalCreateRequest;
import com.wootecam.festivals.domain.festival.dto.FestivalDetailResponse;
import com.wootecam.festivals.domain.festival.dto.FestivalIdResponse;
import com.wootecam.festivals.domain.festival.dto.FestivalListResponse;
import com.wootecam.festivals.domain.festival.dto.KeySetPageResponse;
import com.wootecam.festivals.domain.festival.service.FestivalService;
import com.wootecam.festivals.global.api.ApiResponse;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/festivals")
@RequiredArgsConstructor
public class FestivalController {

    private final FestivalService festivalService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ApiResponse<FestivalIdResponse> createFestival(@Valid @RequestBody FestivalCreateRequest requestDto) {
        return ApiResponse.of(festivalService.createFestival(requestDto));
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{festivalId}")
    public ApiResponse<FestivalDetailResponse> getFestival(@PathVariable Long festivalId) {
        return ApiResponse.of(festivalService.getFestivalDetail(festivalId));
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public ApiResponse<KeySetPageResponse<FestivalListResponse>> getFestivals(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cursorTime,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "10") int pageSize) {
        // DateTimeFormat을 통해 2023-05-23T10:15:30 이런 형태의 날짜-시간 문자열을 LocalDateTIme으로 파싱
        return ApiResponse.of(festivalService.getFestivals(cursorTime, cursorId, pageSize));
    }
}