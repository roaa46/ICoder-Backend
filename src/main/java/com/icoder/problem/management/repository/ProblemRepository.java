package com.icoder.problem.management.repository;

import com.icoder.problem.management.entity.Problem;
import com.icoder.problem.management.enums.OJudgeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long>, JpaSpecificationExecutor<Problem> {

    Optional<Problem> findByProblemCodeAndOnlineJudge(String code, OJudgeType source);
}
