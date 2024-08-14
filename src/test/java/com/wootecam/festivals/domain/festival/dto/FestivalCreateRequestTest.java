package com.wootecam.festivals.domain.festival.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class FestivalCreateRequestTest {

    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();

    private static Stream<Arguments> invalidDtoProvider() {
        LocalDateTime now = LocalDateTime.now();
        return Stream.of(
                Arguments.of(null, "Title", "Description", now.plusDays(1), now.plusDays(2), "주최 단체 정보는 필수입니다."),
                Arguments.of(1L, "", "Description", now.plusDays(1), now.plusDays(2), "축제 제목은 필수입니다."),
                Arguments.of(1L, "Title", "", now.plusDays(1), now.plusDays(2), "축제 설명은 필수입니다."),
                Arguments.of(1L, "Title", "Description", now.minusDays(1), now.plusDays(2), "시작 시간은 현재보다 미래여야 합니다."),
                Arguments.of(1L, "Title", "Description", now.plusDays(2), now.plusDays(1), "종료 시간은 시작 시간보다 늦어야 합니다.")
        );
    }

    @Test
    @DisplayName("유효한 데이터로 DTO 생성 시 검증 통과")
    void validDtoShouldPass() {
        LocalDateTime now = LocalDateTime.now();
        FestivalCreateRequest dto = new FestivalCreateRequest(
                1L, "Summer Festival", "A great summer festival",
                now.plusDays(1), now.plusDays(2)
        );

        var violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("invalidDtoProvider")
    @DisplayName("잘못된 데이터로 DTO 생성 시 검증 실패")
    void invalidDtoShouldFail(Long organizationId, String title, String description,
                              LocalDateTime startTime, LocalDateTime endTime, String expectedViolation) {
        FestivalCreateRequest dto = new FestivalCreateRequest(
                organizationId, title, description, startTime, endTime
        );

        var violations = validator.validate(dto);
        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().map(v -> v.getMessage()).toList()).contains(expectedViolation);
    }
}