package com.wootecam.festivals.domain.organization.entity;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Organization 테스트")
class OrganizationTest {

    @Nested
    @DisplayName("Organization 생성 시")
    class create_organization {

        @Nested
        @DisplayName("이름, 프로필 이미지, 설명이 주어지면")
        class when_name_profileImg_detail_is_given {

            String name = "ValidName";
            String profileImg = "profile.jpg";
            String detail = "This is a valid detail.";

            @Test
            @DisplayName("Organization을 생성한다")
            void it_returns_new_organization() {
                Organization organization = Organization.builder()
                        .name(name)
                        .profileImg(profileImg)
                        .detail(detail)
                        .build();

                assertAll(() -> {
                    assertNotNull(organization);
                    assertEquals(name, organization.getName());
                    assertEquals(profileImg, organization.getProfileImg());
                    assertEquals(detail, organization.getDetail());
                    assertFalse(organization.isDeleted());
                });
            }

            @DisplayName("이름이 1자 이하라면 예외가 발생한다")
            @Test
            void it_returns_exception_name_length_is_less_then_1() {
                String name = "";

                assertThrows(IllegalArgumentException.class, () -> {
                    Organization.builder()
                            .name(name)
                            .profileImg(profileImg)
                            .detail(detail)
                            .build();
                });
            }

            @DisplayName("이름이 20자 초과라면 예외가 발생한다")
            @Test
            void it_returns_exception_name_length_is_more_then_20() {
                String name = "a".repeat(21);

                assertThrows(IllegalArgumentException.class, () -> {
                    Organization.builder()
                            .name(name)
                            .profileImg(profileImg)
                            .detail(detail)
                            .build();
                });
            }

            @DisplayName("설명이 200자 초과라면 예외가 발생한다")
            @Test
            void it_returns_exception_detail_length_is_more_then_200() {
                String detail = "a".repeat(201);

                assertThrows(IllegalArgumentException.class, () -> {
                    Organization.builder()
                            .name(name)
                            .profileImg(profileImg)
                            .detail(detail)
                            .build();
                });
            }
        }

        @Nested
        @DisplayName("이름이 주어지면")
        class when_name_is_given {

            String name = "ValidName";

            @Test
            @DisplayName("Organization을 생성한다")
            void it_returns_new_organization() {
                Organization organization = Organization.builder()
                        .name(name)
                        .profileImg(null)
                        .detail(null)
                        .build();

                assertAll(() -> {
                    assertNotNull(organization);
                    assertEquals(name, organization.getName());
                    assertFalse(organization.isDeleted());
                });
            }
        }
    }
}