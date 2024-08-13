package com.wootecam.festivals.domain.organization.repository;

import com.wootecam.festivals.domain.organization.entity.OrganizationMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, Long> {
}
