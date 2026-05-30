package com.icoder.contest.management.repository;

import com.icoder.contest.management.entity.ContestUserRelation;
import com.icoder.contest.management.enums.ContestRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContestUserRelationRepository extends JpaRepository<ContestUserRelation, Long> {
    Optional<ContestUserRelation> findByContestIdAndRole(Long contestId, ContestRole role);

    Optional<ContestUserRelation> findByContestIdAndUserId(Long contestLd, Long userId);

    @Query("SELECT r FROM ContestUserRelation r JOIN FETCH r.user " +
            "WHERE r.contest.id = :contestId " +
            "ORDER BY r.score DESC, r.penalty ASC")
    List<ContestUserRelation> findAllByContestIdOrderByRank(@Param("contestId") Long contestId);
}
