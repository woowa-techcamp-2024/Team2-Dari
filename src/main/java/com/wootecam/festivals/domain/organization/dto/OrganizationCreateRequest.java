package com.wootecam.festivals.domain.organization.dto;

import com.wootecam.festivals.domain.organization.entity.Organization;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OrganizationCreateRequest(
        @NotNull(message = "조직 이름은 필수입니다.")
        @Size(min = 1, max = 20, message = "조직 이름은 1자 이상 20자 이하여야 합니다.")
        String name,

        @Size(max = 200, message = "조직 설명은 200자 이하여야 합니다.")
        String detail,
        String profileImg) {

    public Organization toEntity() {
        return Organization.builder()
                .name(this.name())
                .detail(this.detail())
                .profileImg(this.profileImg())
                .build();
    }
}
