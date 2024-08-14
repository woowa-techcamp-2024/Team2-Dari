package com.wootecam.festivals.domain.festival.util;

import com.wootecam.festivals.domain.festival.dto.FestivalCreateRequest;
import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.organization.entity.Organization;
import com.wootecam.festivals.domain.organization.repository.OrganizationRepository;
import com.wootecam.festivals.global.exception.GlobalErrorCode;
import com.wootecam.festivals.global.exception.type.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FestivalFactory {

    private final OrganizationRepository organizationRepository;

    public Festival createFromDto(FestivalCreateRequest request) {
        Organization organization = organizationRepository.findById(request.organizationId())
                .orElseThrow(() -> new ApiException(GlobalErrorCode.INVALID_REQUEST_PARAMETER, "유효하지 않는 조직입니다."));

        return Festival.builder()
                .organization(organization)
                .title(request.title())
                .description(request.description())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .build();
    }
}
