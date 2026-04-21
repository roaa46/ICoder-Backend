package com.icoder.activity.management.repository;

import com.icoder.activity.management.entity.ActivityLog;
import com.icoder.submission.management.enums.SubmissionVerdict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

}
