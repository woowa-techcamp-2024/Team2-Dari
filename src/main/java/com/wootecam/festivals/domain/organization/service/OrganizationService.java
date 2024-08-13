package com.wootecam.festivals.domain.organization.service;

import com.wootecam.festivals.domain.organization.dto.OrganizationCreateDto;
import com.wootecam.festivals.domain.organization.entity.Organization;
import com.wootecam.festivals.domain.organization.entity.OrganizationMember;
import com.wootecam.festivals.domain.organization.entity.OrganizationRole;
import com.wootecam.festivals.domain.organization.repository.OrganizationMemberRepository;
import com.wootecam.festivals.domain.organization.repository.OrganizationRepository;
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
    public Long createOrganization(OrganizationCreateDto organizationCreateDto) {
        Organization newOrganization = saveOrganization(organizationCreateDto);
        registerOrganizationAdmin(newOrganization);

        return newOrganization.getId();
    }

    private Organization saveOrganization(OrganizationCreateDto organizationCreateDto) {
        return organizationRepository.save(organizationCreateDto.toEntity());
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
