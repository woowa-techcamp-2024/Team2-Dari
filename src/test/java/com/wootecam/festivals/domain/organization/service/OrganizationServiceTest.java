package com.wootecam.festivals.domain.organization.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.wootecam.festivals.domain.member.repository.MemberRepository;
import com.wootecam.festivals.domain.organization.dto.OrganizationCreateDto;
import com.wootecam.festivals.domain.organization.repository.OrganizationRepository;
import com.wootecam.festivals.utils.SpringBootTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("OrganizationService 테스트")
class OrganizationServiceTest extends SpringBootTestConfig {

    private final MemberRepository memberRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationService organizationService;

    @Autowired
    public OrganizationServiceTest(MemberRepository memberRepository, OrganizationRepository organizationRepository,
                                   OrganizationService organizationService) {
        this.memberRepository = memberRepository;
        this.organizationRepository = organizationRepository;
        this.organizationService = organizationService;
    }

    @BeforeEach
    void setUp() {
        organizationRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Nested
    @DisplayName("Organization 생성 시")
    class create_organization {

        @Nested
        @DisplayName("유효한 이름, 프로필 이미지, 설명이 주어지면")
        class when_valid_name_profileImg_detail_is_given {

            OrganizationCreateDto givenOrganizationCreateDto
                    = new OrganizationCreateDto("validName", "profile.jpg", "This is a valid detail.");

            @Test
            @DisplayName("Organization을 생성한다")
            void it_returns_new_organization() {
                Long organizationId = organizationService.createOrganization(givenOrganizationCreateDto);

                assertAll(() -> {
                    assertNotNull(organizationId);
                });
            }
        }
    }
}