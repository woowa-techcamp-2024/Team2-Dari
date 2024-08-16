package com.wootecam.festivals.domain.festival.repository;

import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.festival.entity.FestivalProgressStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FestivalRepository extends JpaRepository<Festival, Long> {

    @Query("SELECT f FROM Festival f WHERE f.id = :id AND f.isDeleted = false")
    @Override
    Optional<Festival> findById(Long id);

    @Query("SELECT f FROM Festival f JOIN FETCH f.admin " +
            "WHERE (f.startTime > :startTime OR (f.startTime = :startTime AND f.id < :id)) " +
            "AND f.isDeleted = false " +
            "AND f.festivalPublicationStatus != 'DRAFT' " +
            "AND f.startTime > :now " +
            "ORDER BY f.startTime ASC, f.id DESC " +
            "LIMIT :limit")
    List<Festival> findUpcomingFestivalsBeforeCursor(@Param("startTime") LocalDateTime startTime,
                                                     @Param("id") Long id,
                                                     @Param("now") LocalDateTime now,
                                                     @Param("limit") int limit
    );

    @Modifying
    @Query("UPDATE Festival f SET f.festivalProgressStatus = :festivalProgressStatus WHERE f.startTime <= :now AND f.endTime >= :now")
    void bulkUpdateFestivalStatusFestivals(FestivalProgressStatus festivalProgressStatus, LocalDateTime now);

    @Query("SELECT f FROM Festival f WHERE f.festivalProgressStatus != 'COMPLETED' AND f.isDeleted = false")
    List<Festival> findFestivalsWithRestartScheduler();
}
