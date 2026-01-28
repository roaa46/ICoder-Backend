package com.icoder.contest.management.repository;

import com.icoder.contest.management.entity.ContestProblemRelation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContestProblemRelationRepository extends JpaRepository<ContestProblemRelation, Long> {
}
