package com.wootecam.festivals.domain.festival.service;

import com.wootecam.festivals.domain.festival.dto.ParticipantResponse;
import com.wootecam.festivals.domain.festival.dto.ParticipantsPaginationResponse;
import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.festival.exception.FestivalErrorCode;
import com.wootecam.festivals.domain.festival.repository.FestivalRepository;
import com.wootecam.festivals.global.exception.type.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FestivalParticipantService {

    private final FestivalRepository festivalRepository;

    public ParticipantsPaginationResponse getParticipantListWithPagination(Long requestMemberId, Long festivalId,
                                                                           Pageable pageable) {
        checkFestivalAuth(requestMemberId, festivalId);

        Page<ParticipantResponse> participantsWithPagination = festivalRepository.findParticipantsWithPagination(
                festivalId, pageable);
        log.debug("페스티벌 참가자 리스트 페이지네이션 결과: {}", participantsWithPagination);
        log.debug("페이지 정보: {}", participantsWithPagination.getPageable());

        ParticipantsPaginationResponse response = ParticipantsPaginationResponse.from(participantsWithPagination);
        log.debug("페스티벌 참가자 리스트 페이지네이션 응답: participantSize={}, response={}", response.participants().size(), response);

        return response;
    }

    private void checkFestivalAuth(Long requestMemberId, Long festivalId) {
        Festival festival = festivalRepository.findByIdWithAdminMember(festivalId)
                .orElseThrow(() -> {
                    log.error("페스티벌 참가자 리스트 페이지네이션: 페스티벌을 찾을 수 없습니다. festivalId={}", festivalId);
                    return new ApiException(FestivalErrorCode.FESTIVAL_NOT_FOUND);
                });

        Long adminId = festival.getAdmin().getId();
        log.debug("페스티벌 참가자 리스트 페이지네이션: 페스티벌 권한 확인. festivalId={}, adminId={}, requestMemberId={}", festivalId, adminId,
                requestMemberId);
        if (!adminId.equals(requestMemberId)) {
            log.error("페스티벌 참가자 리스트 페이지네이션: 페스티벌에 대한 권한이 없습니다. festivalId={}, adminId={}, requestMemberId={}",
                    festivalId, adminId, requestMemberId);
            throw new ApiException(FestivalErrorCode.FESTIVAL_NOT_AUTHORIZED);
        }
    }
}
