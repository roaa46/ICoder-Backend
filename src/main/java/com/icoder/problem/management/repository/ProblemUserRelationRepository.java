package com.icoder.problem.management.repository;

import com.icoder.problem.management.entity.Problem;
import com.icoder.problem.management.entity.ProblemUserRelation;
import com.icoder.user.management.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProblemUserRelationRepository extends JpaRepository<ProblemUserRelation, Long> {

    Optional<ProblemUserRelation> findByUserIdAndProblemId(Long userId, Long problemId);

    @EntityGraph(attributePaths = "problem")
    Page<ProblemUserRelation> findByUserIdAndSolvedTrue(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = "problem")
    Page<ProblemUserRelation> findByUserIdAndAttemptedTrue(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = "problem")
    Page<ProblemUserRelation> findByUserIdAndFavoriteTrue(Long userId, Pageable pageable);

    List<ProblemUserRelation> findByUserIdAndProblemIn(Long userId, List<Problem> problems);

    Optional<ProblemUserRelation> findByUserAndProblem(User user, Problem problem);

    boolean existsByUserIdAndProblemId(Long userId, Long problemId);
}
