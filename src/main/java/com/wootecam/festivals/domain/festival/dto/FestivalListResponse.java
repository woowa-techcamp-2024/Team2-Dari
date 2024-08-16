package com.wootecam.festivals.domain.festival.dto;

import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.festival.entity.FestivalStatus;
import com.wootecam.festivals.domain.organization.dto.OrganizationResponse;
import java.time.LocalDateTime;

public record FestivalListResponse(Long festivalId,
                                   String title,
                                   LocalDateTime startTime,
                                   LocalDateTime endTime,
                                   FestivalStatus festivalStatus,
                                   OrganizationResponse organization) {

    public static FestivalListResponse from(Festival festival) {
        return new FestivalListResponse(festival.getId(),
                festival.getTitle(),
                festival.getStartTime(),
                festival.getEndTime(),
                festival.getFestivalStatus(),
                OrganizationResponse.from(festival.getOrganization())
        );
    }
}
