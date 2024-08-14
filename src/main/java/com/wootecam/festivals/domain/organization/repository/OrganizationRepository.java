package com.wootecam.festivals.domain.organization.repository;

import com.wootecam.festivals.domain.organization.entity.Organization;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    @Query("select o from Organization o where o.id = :organizationId and o.isDeleted = false")
    Optional<Organization> findById(Long organizationId);
}
