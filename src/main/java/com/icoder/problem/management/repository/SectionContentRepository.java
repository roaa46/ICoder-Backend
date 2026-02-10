package com.icoder.problem.management.repository;

import com.icoder.problem.management.entity.SectionContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SectionContentRepository extends JpaRepository<SectionContent, Long> {
}
