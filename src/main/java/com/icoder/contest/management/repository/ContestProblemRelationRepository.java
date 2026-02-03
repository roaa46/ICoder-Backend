package com.icoder.contest.management.repository;

import com.icoder.contest.management.entity.ContestProblemRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ContestProblemRelationRepository extends JpaRepository<ContestProblemRelation, Long> {
    @Modifying
    @Query("DELETE FROM ContestProblemRelation c WHERE c.contest.id = :contestId")
    void deleteByContestId(Long contestId);
}
