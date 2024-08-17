package com.wootecam.festivals.domain.member.entity;

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
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(name = "member_name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "profile_img", nullable = false)
    private String profileImg;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @Builder
    private Member(String name, String email, String profileImg) {
        this.name = Objects.requireNonNull(name, "회원 이름은 필수입니다.");
        this.email = Objects.requireNonNull(email, "이메일은 필수입니다.");
        this.profileImg = Objects.requireNonNull(profileImg, "프로필 이미지는 필수입니다.");
        this.isDeleted = false;
    }

    public void updateStatusDeleted() {
        isDeleted = true;
    }
}
