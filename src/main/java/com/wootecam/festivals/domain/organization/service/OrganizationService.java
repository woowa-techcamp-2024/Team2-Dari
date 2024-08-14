package com.wootecam.festivals.domain.organization.service;

import com.wootecam.festivals.domain.organization.dto.OrganizationCreateRequest;
import com.wootecam.festivals.domain.organization.dto.OrganizationResponse;
import com.wootecam.festivals.domain.organization.entity.Organization;
import com.wootecam.festivals.domain.organization.entity.OrganizationMember;
import com.wootecam.festivals.domain.organization.entity.OrganizationRole;
import com.wootecam.festivals.domain.organization.exception.OrganizationErrorCode;
import com.wootecam.festivals.domain.organization.repository.OrganizationMemberRepository;
import com.wootecam.festivals.domain.organization.repository.OrganizationRepository;
import com.wootecam.festivals.global.exception.type.ApiException;
import com.wootecam.festivals.global.utils.AuthenticationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;

    @Transactional
    public Long createOrganization(OrganizationCreateRequest organizationCreateRequest) {
        Organization newOrganization = saveOrganization(organizationCreateRequest);
        registerOrganizationAdmin(newOrganization);

        return newOrganization.getId();
    }

    public OrganizationResponse findOrganization(Long organizationId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ApiException(OrganizationErrorCode.ORGANIZATION_NOT_FOUND));

        return OrganizationResponse.from(organization);
    }

    private Organization saveOrganization(OrganizationCreateRequest organizationCreateRequest) {
        return organizationRepository.save(organizationCreateRequest.toEntity());
    }

    private void registerOrganizationAdmin(Organization newOrganization) {
        Long loginMemberId = AuthenticationUtils.getLoginMemberId();
        OrganizationMember organizationMember = OrganizationMember.builder()
                .organizationId(newOrganization.getId())
                .memberId(loginMemberId)
                .role(OrganizationRole.ADMIN)
                .build();
        organizationMemberRepository.save(organizationMember);
    }
}
