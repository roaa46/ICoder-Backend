package com.icoder.problem.management.repository;

import com.icoder.problem.management.entity.ProblemProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProblemPropertyRepository extends JpaRepository<ProblemProperty, Long> {
}
