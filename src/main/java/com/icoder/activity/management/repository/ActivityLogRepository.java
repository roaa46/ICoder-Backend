package com.icoder.activity.management.repository;

import com.icoder.activity.management.entity.ActivityLog;
import com.icoder.submission.management.enums.SubmissionVerdict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    @Query(value = "SELECT a FROM ActivityLog a JOIN FETCH a.user u " +
            "WHERE u.id = :userId AND (:verdict IS NULL OR a.verdict = :verdict)",
            countQuery = "SELECT count(a) FROM ActivityLog a " +
                    "WHERE a.user.id = :userId AND (:verdict IS NULL OR a.verdict = :verdict)")
    Page<ActivityLog> findByUserWithVerdict(
            @Param("userId") Long userId,
            @Param("verdict") SubmissionVerdict verdict,
            Pageable pageable);

    @Query("SELECT a.createdAt FROM ActivityLog a WHERE a.user.id = :userId")
    List<Instant> findAllActivityInstantsByUserId(@Param("userId") Long userId);

    @Query("SELECT a FROM ActivityLog a " +
            "WHERE a.user.id = :userId " +
            "AND a.createdAt BETWEEN :startDate AND :endDate")
    List<ActivityLog> findUserActivitiesInRange(
            @Param("userId") Long userId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

}
