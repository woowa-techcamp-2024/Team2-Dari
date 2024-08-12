package com.wootecam.festivals.domain.member.entity;

import com.wootecam.festivals.global.BaseEntity;
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
    private String memberName;
    private String email;
    private String profileImg;
    private boolean isDeleted;

    @Builder
    private Member(String memberName, String email, String profileImg) {
        this.memberName = Objects.requireNonNull(memberName, "memberName must be provided.");
        this.email = Objects.requireNonNull(email, "email must be provided.");
        this.profileImg = profileImg;
        this.isDeleted = false;
    }
}
