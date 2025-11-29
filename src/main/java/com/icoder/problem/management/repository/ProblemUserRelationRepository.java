package com.icoder.problem.management.repository;

import com.icoder.problem.management.entity.ProblemUserRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProblemUserRelationRepository extends JpaRepository<ProblemUserRelation, Long> {
}
