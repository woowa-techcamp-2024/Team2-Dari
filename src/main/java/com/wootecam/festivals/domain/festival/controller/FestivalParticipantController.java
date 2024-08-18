package com.wootecam.festivals.domain.festival.controller;


import com.wootecam.festivals.domain.festival.dto.ParticipantsPaginationResponse;
import com.wootecam.festivals.domain.festival.service.FestivalParticipantService;
import com.wootecam.festivals.global.api.ApiResponse;
import com.wootecam.festivals.global.auth.AuthUser;
import com.wootecam.festivals.global.auth.Authentication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/*
 * 축제 참가자 관련 API를 처리하는 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/festivals/{festivalId}/participants")
@RequiredArgsConstructor
public class FestivalParticipantController {

    private final FestivalParticipantService festivalParticipantService;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public ApiResponse<ParticipantsPaginationResponse> getParticipants(@AuthUser Authentication authentication,
                                                                       @PathVariable Long festivalId,
                                                                       @RequestParam int page,
                                                                       @RequestParam int size) {
        Long requestMemberId = authentication.memberId();
        Pageable pageable = PageRequest.of(page, size);
        log.debug("페스티벌 참가자 리스트 페이지네이션: requestMemberId={}, festivalId={}, pageable={}", requestMemberId, festivalId,
                pageable);

        ParticipantsPaginationResponse response =
                festivalParticipantService.getParticipantListWithPagination(requestMemberId, festivalId, pageable);
        log.debug("페스티벌 참가자 리스트 페이지네이션 응답: {}", response);

        return ApiResponse.of(response);
    }
}
