package com.wootecam.festivals.domain.organization.service;

import static com.wootecam.festivals.global.utils.AuthenticationUtils.invalidateAuthentication;
import static com.wootecam.festivals.global.utils.AuthenticationUtils.setAuthenticated;
import static com.wootecam.festivals.utils.Fixture.createOrganization;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.wootecam.festivals.domain.member.repository.MemberRepository;
import com.wootecam.festivals.domain.organization.dto.OrganizationCreateRequest;
import com.wootecam.festivals.domain.organization.dto.OrganizationIdResponse;
import com.wootecam.festivals.domain.organization.dto.OrganizationResponse;
import com.wootecam.festivals.domain.organization.entity.Organization;
import com.wootecam.festivals.domain.organization.entity.OrganizationMember;
import com.wootecam.festivals.domain.organization.entity.OrganizationRole;
import com.wootecam.festivals.domain.organization.repository.OrganizationMemberRepository;
import com.wootecam.festivals.domain.organization.repository.OrganizationRepository;
import com.wootecam.festivals.global.auth.Authentication;
import com.wootecam.festivals.utils.SpringBootTestConfig;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("OrganizationService 테스트")
class OrganizationServiceTest extends SpringBootTestConfig {

    private final MemberRepository memberRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final OrganizationService organizationService;

    @Autowired
    public OrganizationServiceTest(MemberRepository memberRepository, OrganizationRepository organizationRepository,
                                   OrganizationMemberRepository organizationMemberRepository,
                                   OrganizationService organizationService) {
        this.memberRepository = memberRepository;
        this.organizationRepository = organizationRepository;
        this.organizationMemberRepository = organizationMemberRepository;
        this.organizationService = organizationService;
    }

    @BeforeEach
    void setUp() {
        organizationRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Nested
    @DisplayName("로그인한 사용자가 Organization 생성 시")
    class create_organization {

        Authentication authentication = new Authentication(1L, "testUser", "testEmail");

        @BeforeEach
        void setUp() {
            setAuthenticated(authentication);
        }

        @AfterEach
        void tearDown() {
            invalidateAuthentication();
        }

        @Nested
        @DisplayName("유효한 이름, 프로필 이미지, 설명이 주어지면")
        class when_valid_name_profileImg_detail_is_given {

            OrganizationCreateRequest givenOrganizationCreateRequest
                    = new OrganizationCreateRequest("validName", "profile.jpg", "This is a valid detail.");

            @Test
            @DisplayName("Organization이 생성되고, 로그인한 사용자가 Admin으로 등록된다.")
            void it_returns_new_organization() {
                OrganizationIdResponse organizationIdResponse = organizationService.createOrganization(
                        givenOrganizationCreateRequest);

                assertAll(() -> {
                    assertThat(organizationIdResponse).isNotNull();
                    assertThat(
                            organizationMemberRepository.findByOrganizationId(organizationIdResponse.organizationId()))
                            .hasSize(1)
                            .extracting(OrganizationMember::getRole)
                            .isEqualTo(List.of(OrganizationRole.ADMIN));
                });
            }
        }
    }

    @Nested
    @DisplayName("Organization 조회 시")
    class find_organization {

        Organization organization;
        Long organizationId;

        @BeforeEach
        void setUp() {
            organization = createOrganization("validName", "profile.jpg", "This is a valid detail.");
            organizationId = organizationRepository.save(organization).getId();
        }

        @Nested
        @DisplayName("Organization id가 주어지면")
        class when_id_is_given {

            @Test
            @DisplayName("해당 organization 정보를 조회하여 반환한다")
            void it_returns_organization() {
                OrganizationResponse organizationResponse = organizationService.findOrganization(organizationId);

                assertAll(() -> {
                    assertThat(organizationResponse.organizationId()).isEqualTo(organizationId);
                    assertThat(organizationResponse.name()).isEqualTo(organization.getName());
                    assertThat(organizationResponse.detail()).isEqualTo(organization.getDetail());
                    assertThat(organizationResponse.profileImg()).isEqualTo(organization.getProfileImg());
                });
            }
        }
    }
}