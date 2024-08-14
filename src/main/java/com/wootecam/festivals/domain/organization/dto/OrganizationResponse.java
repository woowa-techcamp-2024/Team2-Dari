package com.wootecam.festivals.domain.organization.dto;

import com.wootecam.festivals.domain.organization.entity.Organization;

public record OrganizationResponse(Long organizationId,
                                   String name,
                                   String detail,
                                   String profileImg) {

    public static OrganizationResponse from(Organization organization) {
        return new OrganizationResponse(organization.getId(),
                organization.getName(),
                organization.getDetail(),
                organization.getProfileImg());
    }
}
