package com.icoder.problem.management.repository;

import com.icoder.problem.management.entity.Problem;
import com.icoder.problem.management.entity.ProblemUserRelation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProblemUserRelationRepository extends JpaRepository<ProblemUserRelation, Long> {
    
    Optional<ProblemUserRelation> findByUserIdAndProblemId(Long userId, Long problemId);

    Page<ProblemUserRelation> findByUserIdAndIsSolvedTrue(Long userId, Pageable pageable);

    Page<ProblemUserRelation> findByUserIdAndIsAttemptedTrue(Long userId, Pageable pageable);

    Page<ProblemUserRelation> findByUserIdAndIsFavoriteTrue(Long userId, Pageable pageable);

    List<ProblemUserRelation> findByUserIdAndProblemIn(Long userId, List<Problem> problems);
}
