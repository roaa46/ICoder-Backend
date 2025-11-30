package com.icoder.group.management.repository;

import com.icoder.group.management.entity.Group;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GroupRepository extends JpaRepository<Group, Long> {
    boolean existsByCode(String code);

    @Query("SELECT g FROM Group g WHERE g.owner.handle = :handle")
    Page<Group> getMyGroups(@Param("handle") String handle, Pageable pageable);
}
