package com.icoder.contest.management.repository;

import com.icoder.contest.management.entity.Contest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContestRepository extends JpaRepository<Contest, Long> {
    boolean existsByIdAndGroupId(Long contestId, Long groupId);
}
