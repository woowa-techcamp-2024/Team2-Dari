package com.wootecam.festivals.domain.festival.entity;

import com.wootecam.festivals.global.audit.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Festival extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "festival_id")
    private Long id;
    private Long organizationId; //TODO: Organization으로 ManyToOne 변경 예정
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Builder
    public Festival(Long organizationId, String title, String description, LocalDateTime startTime,
                    LocalDateTime endTime) {
        this.organizationId = Objects.requireNonNull(organizationId);
        this.title = Objects.requireNonNull(title);
        this.description = Objects.requireNonNull(description);
        this.startTime = Objects.requireNonNull(startTime);
        this.endTime = Objects.requireNonNull(endTime);
    }
}
