package com.icoder.submission.management.repository;

import com.icoder.submission.management.dto.SubmissionSummary;
import com.icoder.submission.management.entity.Submission;
import com.icoder.submission.management.enums.SubmissionVerdict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long>, JpaSpecificationExecutor<Submission> {

    Optional<Submission> findById(Long id);

    @Query("SELECT s FROM Submission s JOIN FETCH s.problem WHERE s.id = :id")
    Optional<Submission> findByIdWithProblem(@Param("id") Long id);

    @Query("SELECT s FROM Submission s JOIN FETCH s.problem JOIN FETCH s.user WHERE s.id = :id")
    Optional<Submission> findByIdWithProblemAndUser(@Param("id") Long id);

    Page<Submission> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"problem", "user"})
    Page<Submission> findAll(Specification<Submission> spec, Pageable pageable);

    boolean existsByUserIdAndContestIdAndProblemIdAndVerdictAndIdNot(Long user, Long contestId, Long problemId, SubmissionVerdict submissionVerdict, Long submissionId);

    int countByUserIdAndContestIdAndProblemIdAndVerdictIn(Long userId, Long contestId, Long problemId, List<SubmissionVerdict> penaltyVerdicts);

    @Query("SELECT s FROM Submission s " +
            "JOIN FETCH s.problem " +
            "WHERE s.user.id = :userId " +
            "AND s.verdict IS NOT NULL " +
            "AND s.verdict <> com.icoder.submission.management.enums.SubmissionVerdict.PENDING " +
            "AND s.verdict <> com.icoder.submission.management.enums.SubmissionVerdict.RUNNING " +
            "AND s.verdict <> com.icoder.submission.management.enums.SubmissionVerdict.IN_QUEUE")
    List<Submission> findCompletedSubmissionsByUserId(@Param("userId") Long userId);

    @Query("SELECT s.id as id, s.user.id as userId, s.problem.id as problemId, " +
            "s.verdict as verdict, s.submittedAt as createdAt " +
            "FROM Submission s WHERE s.contest.id = :contestId " +
            "ORDER BY s.submittedAt ASC")
    List<SubmissionSummary> findAllByContestIdOrderByCreatedAtAsc(@Param("contestId") Long contestId);

    @Query("SELECT s.problem.id FROM Submission s " +
            "WHERE s.user.id = :userId " +
            "AND s.contest.id = :contestId " +
            "AND s.verdict = 'ACCEPTED'")
    Set<Long> findSolvedProblemIdsByUserIdAndContestId(
            @Param("userId") Long userId,
            @Param("contestId") Long contestId);

    boolean existsByUserIdAndProblemIdAndContestIdAndVerdict(Long userId, Long problemId, Long contestId, SubmissionVerdict verdict);
}
