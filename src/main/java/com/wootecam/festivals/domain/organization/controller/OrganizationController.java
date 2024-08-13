package com.wootecam.festivals.domain.organization.controller;

import com.wootecam.festivals.domain.organization.dto.OrganizationCreateDto;
import com.wootecam.festivals.domain.organization.dto.OrganizationIdDto;
import com.wootecam.festivals.domain.organization.service.OrganizationService;
import com.wootecam.festivals.global.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ApiResponse<OrganizationIdDto> createOrganization(@RequestBody OrganizationCreateDto organizationCreateDto) {
        Long organizationId = organizationService.createOrganization(organizationCreateDto);

        return ApiResponse.of(new OrganizationIdDto(organizationId));
    }
}
