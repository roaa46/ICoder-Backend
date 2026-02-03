package com.icoder.contest.management.repository;

import com.icoder.contest.management.entity.ContestUserRelation;
import com.icoder.contest.management.enums.ContestRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContestUserRelationRepository extends JpaRepository<ContestUserRelation, Long> {
    Optional<ContestUserRelation> findByContestIdAndRole(Long contestId, ContestRole role);
}
