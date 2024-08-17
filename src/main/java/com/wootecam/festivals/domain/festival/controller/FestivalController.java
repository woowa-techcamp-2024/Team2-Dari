package com.wootecam.festivals.domain.festival.controller;

import com.wootecam.festivals.domain.festival.dto.FestivalCreateRequest;
import com.wootecam.festivals.domain.festival.dto.FestivalIdResponse;
import com.wootecam.festivals.domain.festival.dto.FestivalListResponse;
import com.wootecam.festivals.domain.festival.dto.FestivalResponse;
import com.wootecam.festivals.domain.festival.dto.KeySetPageResponse;
import com.wootecam.festivals.domain.festival.service.FestivalService;
import com.wootecam.festivals.global.api.ApiResponse;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

/**
 * 축제 관련 API를 처리하는 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/festivals")
@RequiredArgsConstructor
public class FestivalController {

    private final FestivalService festivalService;

    /**
     * 축제 생성 API
     *
     * @param requestDto 축제 생성 요청 DTO
     * @return 생성된 축제의 ID
     */

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ApiResponse<FestivalIdResponse> createFestival(@Valid @RequestBody FestivalCreateRequest requestDto) {
        log.debug("축제 생성 요청");
        FestivalIdResponse response = festivalService.createFestival(requestDto);
        log.debug("축제 생성 완료 - 축제 ID: {}", response.festivalId());
        return ApiResponse.of(response);
    }

    /**
     * 축제 상세 정보 조회 API
     *
     * @param festivalId 조회할 축제의 ID
     * @return 축제 상세 정보
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{festivalId}")
    public ApiResponse<FestivalResponse> getFestival(@PathVariable Long festivalId) {
        log.debug("축제 상세 정보 조회 요청 - 축제 ID: {}", festivalId);
        FestivalResponse response = festivalService.getFestivalDetail(festivalId);
        log.debug("축제 상세 정보 조회 완료 - 축제 ID: {}", festivalId);
        return ApiResponse.of(response);
    }

    /**
     * 축제 목록 조회 API
     *
     * @param cursorTime 커서 시간 (페이지네이션)
     * @param cursorId   커서 ID (페이지네이션)
     * @param pageSize   페이지 크기
     * @return 축제 목록과 페이지네이션 정보
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public ApiResponse<KeySetPageResponse<FestivalListResponse>> getFestivals(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime cursorTime,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "10") int pageSize) {
        // DateTimeFormat을 통해 2023-05-23T10:15:30 이런 형태의 날짜-시간 문자열을 LocalDateTIme으로 파싱
        log.debug("축제 목록 조회 요청 - cursorTime: {}, cursorId: {}, pageSize: {}", cursorTime, cursorId, pageSize);
        KeySetPageResponse<FestivalListResponse> response = festivalService.getFestivals(cursorTime, cursorId,
                pageSize);
        log.debug("축제 목록 조회 완료 - 조회된 축제 수: {}", response.content().size());
        return ApiResponse.of(response);
    }
}