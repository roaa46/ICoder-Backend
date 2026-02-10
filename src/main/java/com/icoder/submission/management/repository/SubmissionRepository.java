package com.icoder.submission.management.repository;

import com.icoder.submission.management.entity.Submission;
import com.icoder.submission.management.enums.SubmissionVerdict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    Set<Submission> findAllByVerdictIn(List<SubmissionVerdict> inQueue);
    @Query("SELECT s FROM Submission s JOIN FETCH s.problem WHERE s.id = :id")
    Optional<Submission> findByIdWithProblem(@Param("id") Long id);
}
