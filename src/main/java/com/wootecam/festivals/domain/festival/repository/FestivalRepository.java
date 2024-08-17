package com.wootecam.festivals.domain.festival.repository;

import com.wootecam.festivals.domain.festival.dto.FestivalListResponse;
import com.wootecam.festivals.domain.festival.entity.Festival;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FestivalRepository extends JpaRepository<Festival, Long> {

    @Query("SELECT f FROM Festival f WHERE f.id = :id AND f.isDeleted = false")
    @Override
    Optional<Festival> findById(Long id);

    @Query("""
            SELECT new com.wootecam.festivals.domain.festival.dto.FestivalListResponse(
                f.id, f.title, f.startTime, f.endTime, f.festivalPublicationStatus, f.festivalProgressStatus,
                new com.wootecam.festivals.domain.festival.dto.FestivalAdminResponse(
                    f.admin.id, f.admin.name, f.admin.email, f.admin.profileImg
                )
            )
            FROM Festival f
            JOIN f.admin a
            WHERE (f.startTime > :startTime OR (f.startTime = :startTime AND f.id < :id))
                AND f.isDeleted = false
                AND f.festivalPublicationStatus != 'DRAFT'
                AND f.startTime > :now
            ORDER BY f.startTime ASC, f.id DESC
            """)
    List<FestivalListResponse> findUpcomingFestivalsBeforeCursor(@Param("startTime") LocalDateTime startTime,
                                                                 @Param("id") Long id,
                                                                 @Param("now") LocalDateTime now,
                                                                 Pageable pageable
    );

    @Modifying
    @Query("UPDATE Festival f SET f.festivalProgressStatus = 'COMPLETED' WHERE f.festivalProgressStatus != 'COMPLETED' AND f.endTime <= :now")
    void bulkUpdateCOMPLETEDFestivals(LocalDateTime now);

    @Modifying
    @Query("UPDATE Festival f SET f.festivalProgressStatus = 'ONGOING' WHERE f.festivalProgressStatus = 'UPCOMING' AND f.startTime <= :now")
    void bulkUpdateONGOINGFestivals(LocalDateTime now);

    @Query("SELECT f FROM Festival f WHERE f.festivalProgressStatus != 'COMPLETED' AND f.isDeleted = false")
    List<Festival> findFestivalsWithRestartScheduler();
}
