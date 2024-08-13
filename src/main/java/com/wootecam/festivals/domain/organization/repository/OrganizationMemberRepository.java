package com.wootecam.festivals.domain.organization.repository;

import com.wootecam.festivals.domain.organization.entity.OrganizationMember;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, Long> {

    List<OrganizationMember> findByOrganizationId(Long organizationId);
}
