package com.wootecam.festivals.domain.organization.entity;

import com.wootecam.festivals.global.audit.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Organization extends BaseEntity {

    public static final int NAME_MIN_LENGTH = 1;
    public static final int NAME_MAX_LENGTH = 20;
    public static final int DETAIL_MAX_LENGTH = 200;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "organization_id")
    private Long id;
    @Column(name = "member_name", length = NAME_MAX_LENGTH, nullable = false)
    private String name;
    private String profileImg;
    @Column(length = DETAIL_MAX_LENGTH)
    private String detail;
    private boolean isDeleted;

    @Builder
    private Organization(String name, String profileImg, String detail) {
        validateNameLength(name);
        if (detail != null) {
            validateDetailLength(detail);
        }

        this.name = Objects.requireNonNull(name, "organization must be provided.");
        this.profileImg = profileImg;
        this.detail = detail;
        this.isDeleted = false;
    }

    private void validateNameLength(String name) {
        if (name.length() < NAME_MIN_LENGTH) {
            throw new IllegalArgumentException("조직 이름은 1자 이상이어야 합니다.");
        }
        if (name.length() > NAME_MAX_LENGTH) {
            throw new IllegalArgumentException("조직 이름은 20자 이상이어야 합니다.");
        }
    }

    private void validateDetailLength(String detail) {
        if (detail.length() > DETAIL_MAX_LENGTH) {
            throw new IllegalArgumentException("조직 설명은 200자 이하이어야 합니다.");
        }
    }
}