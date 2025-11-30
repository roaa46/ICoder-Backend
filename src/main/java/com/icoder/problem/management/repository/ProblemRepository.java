package com.icoder.problem.management.repository;

import com.icoder.problem.management.enums.OJudgeType;
import com.icoder.problem.management.entity.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {
    Optional<Problem> findByProblemCodeAndOnlineJudge(String code, OJudgeType source);
}
