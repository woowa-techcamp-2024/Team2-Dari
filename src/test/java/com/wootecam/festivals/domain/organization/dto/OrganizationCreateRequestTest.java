package com.wootecam.festivals.domain.organization.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("OrganizationCreateDto 테스트")
class OrganizationCreateRequestTest {

    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();

    private static Stream<Arguments> invalidDtoProvider() {
        return Stream.of(
                Arguments.of(null, "Valid detail", "image.png", "조직 이름은 필수입니다."),
                Arguments.of("", "Valid detail", "image.png", "조직 이름은 1자 이상 20자 이하여야 합니다."),
                Arguments.of("ThisNameIsWayTooLongForValidationToPass", "Valid detail", "image.png",
                        "조직 이름은 1자 이상 20자 이하여야 합니다."),
                Arguments.of("ValidName", "This detail is too long for validation. ".repeat(10), "image.png",
                        "조직 설명은 200자 이하여야 합니다.")
        );
    }

    @Test
    @DisplayName("유효한 데이터로 DTO 생성 시 검증 통과")
    void validDtoShouldPass() {
        OrganizationCreateRequest dto = new OrganizationCreateRequest(
                "Valid Organization", "This is a valid description", "image.png"
        );

        var violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("invalidDtoProvider")
    @DisplayName("잘못된 데이터로 DTO 생성 시 검증 실패")
    void invalidDtoShouldFail(String name, String detail, String profileImg, String expectedViolation) {
        OrganizationCreateRequest dto = new OrganizationCreateRequest(name, detail, profileImg);

        var violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().map(v -> v.getMessage()).toList()).contains(expectedViolation);
    }
}