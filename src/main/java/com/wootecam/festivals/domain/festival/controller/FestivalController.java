package com.wootecam.festivals.domain.festival.controller;

import com.wootecam.festivals.domain.festival.dto.FestivalCreateRequest;
import com.wootecam.festivals.domain.festival.dto.FestivalIdResponse;
import com.wootecam.festivals.domain.festival.dto.FestivalListResponse;
import com.wootecam.festivals.domain.festival.dto.FestivalResponse;
import com.wootecam.festivals.domain.festival.dto.KeySetPageResponse;
import com.wootecam.festivals.domain.festival.service.FestivalService;
import com.wootecam.festivals.global.api.ApiResponse;
import com.wootecam.festivals.global.auth.AuthUser;
import com.wootecam.festivals.global.auth.Authentication;
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
     * @param request 축제 생성 요청 DTO
     * @param authentication 인증 정보
     * @return 생성된 축제의 ID
     */
    //TODO: 인증 실패시 테스트코드 작성
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ApiResponse<FestivalIdResponse> createFestival(@Valid @RequestBody FestivalCreateRequest request,
                                                          @AuthUser Authentication authentication) {
        log.debug("축제 생성 요청");
        FestivalIdResponse response = festivalService.createFestival(request, authentication.memberId());
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
     * 이 API는 커서 기반 페이지네이션을 사용하여 다가오는 축제 목록을 조회합니다.
     *
     * 커서 구현 상세:
     * 1. cursorTime과 cursorId를 함께 사용하는 이유:
     *    - 동일한 시작 시간을 가진 여러 축제가 존재할 수 있습니다.
     *    - 이 경우, cursorTime만으로는 정확한 페이지네이션이 어렵습니다.
     *    - cursorId를 추가로 사용함으로써, 같은 시간에 시작하는 축제들 사이에서도
     *      일관된 순서를 보장할 수 있습니다.
     *
     * 2. 정렬 순서:
     *    - 주 정렬 기준은 축제 시작 시간(오름차순)입니다.
     *    - 시작 시간이 같은 경우, 축제 ID(내림차순)로 추가 정렬합니다.
     *    - 이는 동일 시간대의 축제들 중 가장 최근에 생성된(ID가 큰) 축제부터
     *      표시하기 위함입니다.
     *
     * @param cursorTime 커서 시간 (페이지네이션), 형식: yyyy-MM-dd'T'HH:mm
     * @param cursorId   커서 ID (페이지네이션)
     * @param pageSize   페이지 크기, 기본값: 10
     * @return 축제 목록과 페이지네이션 정보를 포함한 응답
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public ApiResponse<KeySetPageResponse<FestivalListResponse>> getFestivals(
            @RequestParam(name = "time", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
            LocalDateTime cursorTime,
            @RequestParam(name = "id", required = false) Long cursorId,
            @RequestParam(defaultValue = "10") int pageSize) {
        log.debug("축제 목록 조회 요청 - cursorTime: {}, cursorId: {}, pageSize: {}", cursorTime, cursorId, pageSize);
        KeySetPageResponse<FestivalListResponse> response = festivalService.getFestivals(cursorTime, cursorId,
                pageSize);
        log.debug("축제 목록 조회 완료 - 조회된 축제 수: {}", response.content().size());
        return ApiResponse.of(response);
    }
}