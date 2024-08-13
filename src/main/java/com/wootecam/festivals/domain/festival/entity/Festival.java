package com.wootecam.festivals.domain.festival.entity;

import com.wootecam.festivals.global.audit.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
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

    @NotNull
    @Column(name = "organization_id", nullable = false)
    private Long organizationId; //TODO: Organization으로 ManyToOne 변경 예정

    @NotNull
    @Column(nullable = false, length = 100)
    private String title;

    @NotNull
    @Column(nullable = false, length = 2000)
    private String description;

    @NotNull
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @NotNull
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Builder
    private Festival(Long organizationId, String title, String description, LocalDateTime startTime,
                     LocalDateTime endTime) {
        this.organizationId = Objects.requireNonNull(organizationId);
        this.title = Objects.requireNonNull(title);
        this.description = Objects.requireNonNull(description);
        this.startTime = Objects.requireNonNull(startTime);
        this.endTime = Objects.requireNonNull(endTime);
    }
}
