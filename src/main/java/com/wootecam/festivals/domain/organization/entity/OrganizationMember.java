package com.wootecam.festivals.domain.organization.entity;

import com.wootecam.festivals.global.audit.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class OrganizationMember extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "organization_member_id")
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "organization_role", nullable = false)
    @Enumerated(EnumType.STRING)
    private OrganizationRole role;

    @Builder
    private OrganizationMember(Long organizationId, Long memberId, OrganizationRole role) {
        this.organizationId = organizationId;
        this.memberId = memberId;
        this.role = role;
    }
}
