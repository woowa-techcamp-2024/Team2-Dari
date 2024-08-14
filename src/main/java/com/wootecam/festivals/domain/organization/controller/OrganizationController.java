package com.wootecam.festivals.domain.organization.controller;

import com.wootecam.festivals.domain.organization.dto.OrganizationCreateRequest;
import com.wootecam.festivals.domain.organization.dto.OrganizationIdResponse;
import com.wootecam.festivals.domain.organization.dto.OrganizationResponse;
import com.wootecam.festivals.domain.organization.service.OrganizationService;
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
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ApiResponse<OrganizationIdResponse> createOrganization(
            @Valid @RequestBody OrganizationCreateRequest organizationCreateRequest) {
        Long organizationId = organizationService.createOrganization(organizationCreateRequest);

        return ApiResponse.of(new OrganizationIdResponse(organizationId));
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{organizationId}")
    public ApiResponse<OrganizationResponse> findOrganization(@PathVariable Long organizationId) {
        return ApiResponse.of(organizationService.findOrganization(organizationId));
    }
}
