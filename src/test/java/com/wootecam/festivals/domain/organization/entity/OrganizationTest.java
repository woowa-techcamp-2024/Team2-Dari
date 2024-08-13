package com.wootecam.festivals.domain.organization.entity;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class OrganizationTest {

    @Nested
    @DisplayName("Organization 생성 시")
    class create_organization {

        @DisplayName("이름이 1자 이하라면 예외가 발생한다")
        @Test
        void it_returns_exception_name_length_is_less_then_1() {
            // Given
            String name = "";
            String profileImg = "profile.jpg";
            String detail = "This is a valid detail.";

            // When Then
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
            // Given
            String name = "a".repeat(21);
            String profileImg = "profile.jpg";
            String detail = "This is a valid detail.";

            // When Then
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
            // Given
            String name = "name";
            String profileImg = "profile.jpg";
            String detail = "a".repeat(201);

            // When Then
            assertThrows(IllegalArgumentException.class, () -> {
                Organization.builder()
                        .name(name)
                        .profileImg(profileImg)
                        .detail(detail)
                        .build();
            });
        }

        @Nested
        @DisplayName("유효한 이름, 프로필 이미지, 설명이 주어지면")
        class when_valid_name_profileImg_detail_is_given {

            @Test
            @DisplayName("Organization을 생성한다")
            void it_returns_new_organization() {
                // Given
                String name = "ValidName";
                String profileImg = "profile.jpg";
                String detail = "This is a valid detail.";

                // When
                Organization organization = Organization.builder()
                        .name(name)
                        .profileImg(profileImg)
                        .detail(detail)
                        .build();

                // Then
                assertAll(() -> {
                    assertNotNull(organization);
                    assertEquals(name, organization.getName());
                    assertEquals(profileImg, organization.getProfileImg());
                    assertEquals(detail, organization.getDetail());
                    assertFalse(organization.isDeleted());
                });
            }
        }
    }
}