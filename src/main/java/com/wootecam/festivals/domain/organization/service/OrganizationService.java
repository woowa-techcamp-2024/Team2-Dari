package com.wootecam.festivals.domain.organization.service;

import com.wootecam.festivals.domain.organization.dto.OrganizationCreateDto;
import com.wootecam.festivals.domain.organization.entity.Organization;
import com.wootecam.festivals.domain.organization.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    public Long createOrganization(OrganizationCreateDto organizationCreateDto) {
        Organization newOrganization = organizationRepository.save(organizationCreateDto.toEntity());

        return newOrganization.getId();
    }
}
