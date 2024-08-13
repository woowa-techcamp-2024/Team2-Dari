package com.wootecam.festivals.domain.organization.repository;

import com.wootecam.festivals.domain.organization.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
}
