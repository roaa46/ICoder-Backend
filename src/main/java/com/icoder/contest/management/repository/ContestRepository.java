package com.icoder.contest.management.repository;

import com.icoder.contest.management.entity.Contest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Repository
public interface ContestRepository extends JpaRepository<Contest, Long> {
    boolean existsByIdAndGroupId(Long contestId, Long groupId);

    Page<Contest> findByGroupId(Long groupIdLong, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE Contest c SET c.contestStatus = 'RUNNING' " +
            "WHERE c.contestStatus = 'SCHEDULED' AND c.beginTime <= :now")
    void startScheduledContests(Instant now);

    @Modifying
    @Transactional
    @Query("UPDATE Contest c SET c.contestStatus = 'ENDED' " +
            "WHERE c.contestStatus = 'RUNNING' AND c.endTime <= :now")
    void endRunningContests(Instant now);
}
