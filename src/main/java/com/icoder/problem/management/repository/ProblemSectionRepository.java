package com.icoder.problem.management.repository;

import com.icoder.problem.management.entity.ProblemSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProblemSectionRepository extends JpaRepository<ProblemSection, Long> {
}
