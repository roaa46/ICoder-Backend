package com.icoder.submission.management.repository;

import com.icoder.problem.management.entity.Problem;
import com.icoder.problem.management.enums.OJudgeType;
import com.icoder.submission.management.entity.Submission;
import com.icoder.submission.management.enums.SubmissionVerdict;
import com.icoder.user.management.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    Optional<Submission> findById(Long id);

    @Query("SELECT s FROM Submission s JOIN FETCH s.problem WHERE s.id = :id")
    Optional<Submission> findByIdWithProblem(@Param("id") Long id);

    @Query("SELECT s FROM Submission s JOIN FETCH s.user WHERE s.id = :id")
    Optional<Submission> findByIdWithUser(@Param("id") Long id);

    @Query("SELECT s FROM Submission s JOIN FETCH s.problem JOIN FETCH s.user WHERE s.id = :id")
    Optional<Submission> findByIdWithProblemAndUser(@Param("id") Long id);

    Set<Submission> findAllByVerdictIn(List<SubmissionVerdict> verdicts);

    List<Submission> findByUserAndProblem(User user, Problem problem);

    @Query("SELECT COUNT(s) FROM Submission s WHERE s.problem.problemCode = :problemCode " +
            "AND s.problem.onlineJudge = :onlineJudge AND s.verdict = 'ACCEPTED'")
    Integer getSolvedCount(@Param("problemCode") String problemCode,
                           @Param("onlineJudge") OJudgeType onlineJudge);

    @Query(value = "SELECT s FROM Submission s " +
            "JOIN s.user u " +
            "JOIN s.problem p " +
            "WHERE (:userHandle IS NULL OR :userHandle = '' OR u.handle LIKE %:userHandle%) " +
            "AND (:oj IS NULL OR :oj = '' OR CAST(p.onlineJudge AS string) LIKE %:oj%) " +
            "AND (:problemCode IS NULL OR :problemCode = '' OR p.problemCode LIKE %:problemCode%) " +
            "AND (:language IS NULL OR :language = '' OR s.language LIKE %:language%) " +
            "ORDER BY s.submittedAt DESC")
    Page<Submission> filterSubmissions(@Param("userHandle") String userHandle,
                                       @Param("oj") String oj,
                                       @Param("problemCode") String problemCode,
                                       @Param("language") String language,
                                       Pageable pageable);

    Page<Submission> findAll(Pageable pageable);
}
