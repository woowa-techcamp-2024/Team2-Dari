package com.wootecam.festivals.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "이메일은 필수입니다.")
        @Size(min = 1, max = 100, message = "이메일은 1자 이상 100자 이하여야 합니다.")
        String email) {
}
