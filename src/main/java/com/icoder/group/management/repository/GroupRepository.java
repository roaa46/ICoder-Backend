package com.icoder.group.management.repository;

import com.icoder.group.management.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<Group, Long> {
    boolean existsByCode(String code);
}
