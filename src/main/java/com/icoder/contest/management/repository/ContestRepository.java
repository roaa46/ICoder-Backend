package com.icoder.contest.management.repository;

import com.icoder.contest.management.entity.Contest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContestRepository extends JpaRepository<Contest, Long>, JpaSpecificationExecutor<Contest> {
    boolean existsByIdAndGroupId(Long contestId, Long groupId);

    Page<Contest> findByGroupId(Long groupIdLong, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE Contest c SET c.contestStatus = 'RUNNING' WHERE c.id IN :ids")
    void startContestsByIds(@Param("ids") List<Long> ids);

    @Modifying
    @Transactional
    @Query("UPDATE Contest c SET c.contestStatus = 'ENDED' WHERE c.id IN :ids")
    void endContestsByIds(@Param("ids") List<Long> ids);

    @Query("SELECT c.id FROM Contest c WHERE c.contestStatus = 'SCHEDULED' AND c.beginTime <= :now")
    List<Long> findIdsToStart(@Param("now") Instant now);

    @Query("SELECT c.id FROM Contest c WHERE c.contestStatus = 'RUNNING' AND c.endTime <= :now")
    List<Long> findIdsToEnd(@Param("now") Instant now);

    @Query("SELECT c FROM Contest c " +
            "LEFT JOIN FETCH c.group " +
            "LEFT JOIN FETCH c.problemRelation pr " +
            "LEFT JOIN FETCH pr.problem " +
            "WHERE c.id = :contestId")
    Optional<Contest> findByIdWithGroupAndProblems(@Param("contestId") Long contestId);
}
