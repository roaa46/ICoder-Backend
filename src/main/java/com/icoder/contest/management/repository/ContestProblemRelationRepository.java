package com.icoder.contest.management.repository;

import com.icoder.contest.management.entity.ContestProblemRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContestProblemRelationRepository extends JpaRepository<ContestProblemRelation, Long> {

    boolean existsByContestIdAndProblemId(Long contestId, Long problemId);

    Optional<ContestProblemRelation> findByContestIdAndProblemId(Long contestId, Long problemId);

    List<ContestProblemRelation> findByContestId(Long contestId);

    List<ContestProblemRelation> findByContestIdAndProblemIdIn(Long contestId, List<Long> problemIds);

    @Modifying
    @Query("UPDATE ContestProblemRelation cpr SET cpr.attemptedCount = cpr.attemptedCount + 1 WHERE cpr.id = :id")
    void incrementAttempted(@Param("id") Long id);

    @Modifying
    @Query("UPDATE ContestProblemRelation cpr SET cpr.solvedCount = cpr.solvedCount + 1 WHERE cpr.id = :id")
    void incrementSolved(@Param("id") Long id);

    @Modifying
    @Query("UPDATE ContestProblemRelation cpr SET cpr.firstAcceptedSubmission.id = :submissionId " +
            "WHERE cpr.id = :id AND cpr.firstAcceptedSubmission IS NULL")
    int setFirstAcceptedSubmission(@Param("id") Long id, @Param("submissionId") Long submissionId);
}
