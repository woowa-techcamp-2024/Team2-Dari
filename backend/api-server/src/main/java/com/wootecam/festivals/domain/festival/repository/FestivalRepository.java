package com.wootecam.festivals.domain.festival.repository;

import com.wootecam.festivals.domain.festival.dto.FestivalListResponse;
import com.wootecam.festivals.domain.festival.dto.ParticipantResponse;
import com.wootecam.festivals.domain.festival.entity.Festival;
import com.wootecam.festivals.domain.my.dto.MyFestivalResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FestivalRepository extends JpaRepository<Festival, Long> {

    @Query("SELECT f FROM Festival f WHERE f.id = :id AND f.isDeleted = false")
    @Override
    Optional<Festival> findById(Long id);

    @Query("SELECT f FROM Festival f JOIN FETCH f.admin WHERE f.id = :id AND f.isDeleted = false")
    Optional<Festival> findByIdWithAdminMember(Long id);

    @Query("""
            SELECT new com.wootecam.festivals.domain.festival.dto.FestivalListResponse(
                f.id, f.title, f.festivalImg, f.startTime, f.endTime, f.festivalPublicationStatus, f.festivalProgressStatus,
                new com.wootecam.festivals.domain.festival.dto.FestivalAdminResponse(
                    f.admin.id, f.admin.name, f.admin.email, f.admin.profileImg
                )
            )
            FROM Festival f
            JOIN f.admin a
            WHERE (f.startTime > :startTime OR (f.startTime = :startTime AND f.id > :id))
                AND f.isDeleted = false
                AND f.festivalPublicationStatus != 'DRAFT'
            ORDER BY f.startTime ASC, f.id DESC
            """)
    List<FestivalListResponse> findUpcomingFestivalsBeforeCursor(@Param("startTime") LocalDateTime startTime,
                                                                 @Param("id") long id,
                                                                 @Param("now") LocalDateTime now,
                                                                 Pageable pageable);

    @Query(value = """
            SELECT new com.wootecam.festivals.domain.my.dto.MyFestivalResponse(f.id, f.title, f.festivalImg, f.startTime)
            FROM Festival f WHERE f.admin.id = :adminId
                        AND f.isDeleted = false
                        ORDER BY f.startTime DESC, f.id DESC""")
    List<MyFestivalResponse> findFestivalsByAdminOrderStartTimeDesc(Long adminId, Pageable pageable);

    @Query(value = """
            SELECT new com.wootecam.festivals.domain.my.dto.MyFestivalResponse(f.id, f.title, f.festivalImg, f.startTime)
            FROM Festival f WHERE f.admin.id = :adminId
                        AND (f.startTime < :startTime OR (f.startTime = :startTime AND f.id < :beforeId))
                        AND f.isDeleted = false
                        ORDER BY f.startTime DESC, f.id DESC""")
    List<MyFestivalResponse> findFestivalsByAdminAndCursorOrderStartTimeDesc(Long adminId, LocalDateTime startTime,
                                                                             Long beforeId,
                                                                             Pageable pageable);

    @Modifying
    @Query("UPDATE Festival f SET f.festivalProgressStatus = 'COMPLETED' WHERE f.festivalProgressStatus != 'COMPLETED' AND f.endTime <= :now")
    void bulkUpdateCOMPLETEDFestivals(LocalDateTime now);

    @Modifying
    @Query("UPDATE Festival f SET f.festivalProgressStatus = 'ONGOING' WHERE f.festivalProgressStatus = 'UPCOMING' AND f.startTime <= :now")
    void bulkUpdateONGOINGFestivals(LocalDateTime now);

    @Query("SELECT f FROM Festival f WHERE f.festivalProgressStatus != 'COMPLETED' AND f.isDeleted = false")
    List<Festival> findFestivalsWithRestartScheduler();


    @Query(value = """
            SELECT DISTINCT new com.wootecam.festivals.domain.festival.dto.ParticipantResponse(
                p.id, p.name, p.email,
                t.id, t.name,
                pu.id, pu.purchaseTime,
                c.id, c.isChecked
            )
            FROM Festival f 
            INNER JOIN Ticket t ON t.festival.id = f.id AND t.isDeleted = false
            INNER JOIN Member p ON p.isDeleted = false
            INNER JOIN Purchase pu ON pu.ticket.id = t.id AND pu.member.id = p.id
            INNER JOIN Checkin c ON c.ticket.id = t.id AND c.member.id = p.id
            WHERE f.id = :festivalId AND f.isDeleted = false 
            ORDER BY pu.purchaseTime ASC
            """,
            countQuery = """
                    SELECT COUNT(DISTINCT p.id)
                    FROM Festival f
                    INNER JOIN Purchase pu ON pu.ticket.festival.id = f.id
                    INNER JOIN Member p ON p.id = pu.member.id AND p.isDeleted = false
                    WHERE f.id = :festivalId AND f.isDeleted = false
            """)
    Page<ParticipantResponse> findParticipantsWithPagination(Long festivalId, Pageable pageable);

    boolean existsById(Long id);
}
