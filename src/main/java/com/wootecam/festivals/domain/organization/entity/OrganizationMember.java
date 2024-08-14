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
    @Column(name = "organization__member_id")
    private Long id;
    @Column(nullable = false)
    private Long organizationId;
    @Column(nullable = false)
    private Long memberId;
    @Enumerated(EnumType.STRING)
    private OrganizationRole role;

    @Builder
    private OrganizationMember(Long organizationId, Long memberId, OrganizationRole role) {
        this.organizationId = organizationId;
        this.memberId = memberId;
        this.role = role;
    }
}
