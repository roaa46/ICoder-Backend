package com.icoder.problem.management.repository;

import com.icoder.problem.management.entity.ProblemUserRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProblemUserRelationRepository extends JpaRepository<ProblemUserRelation, Long> {
    Optional<ProblemUserRelation> findByUserIdAndProblemId(Long userId, Long problemId);
}
