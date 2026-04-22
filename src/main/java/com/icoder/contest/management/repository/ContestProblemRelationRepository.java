package com.icoder.contest.management.repository;

import com.icoder.contest.management.entity.ContestProblemRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContestProblemRelationRepository extends JpaRepository<ContestProblemRelation, Long> {
    @Modifying
    @Query("DELETE FROM ContestProblemRelation c WHERE c.contest.id = :contestId")
    void deleteByContestId(Long contestId);

    boolean existsByContestIdAndProblemId(Long contestId, Long problemId);

    Optional<ContestProblemRelation> findByContestIdAndProblemId(Long contestId, Long problemId);

    List<ContestProblemRelation> findByContestId(Long contestId);
}
