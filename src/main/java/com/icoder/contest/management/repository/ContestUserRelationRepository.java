package com.icoder.contest.management.repository;

import com.icoder.contest.management.entity.ContestUserRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContestUserRelationRepository extends JpaRepository<ContestUserRelation, Long> {
}
