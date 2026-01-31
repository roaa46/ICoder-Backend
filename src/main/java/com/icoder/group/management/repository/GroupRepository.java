package com.icoder.group.management.repository;

import com.icoder.group.management.entity.Group;
import com.icoder.group.management.enums.Visibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

import java.util.Optional;


public interface GroupRepository extends JpaRepository<Group, Long> {

    Optional<Group> findByCode(String code);
    Page<Group> findByNameContainingIgnoreCaseAndVisibility(String name, Visibility visibility, Pageable pageable);
    @Query("SELECT g FROM Group g JOIN UserGroupRole ugr ON g.id = ugr.group.id JOIN User u ON ugr.user.id = u.id WHERE u.handle = :handle")
    Page<Group> getMyGroups(@Param("handle") String handle, Pageable pageable);

    @Query("SELECT g FROM Group g WHERE g.visibility = :visibility")
    Page<Group> getAllPublicGroups(@Param("visibility") Visibility visibility, Pageable pageable);

    @Query("SELECT g FROM Group g JOIN g.userRoles ur WHERE ur.user.id = :userId AND (" +
            "(g.contestCoordinatorType = 'ALL_MEMBERS') OR " +
            "(g.contestCoordinatorType = 'LEADER_MANAGER' AND ur.role IN ('OWNER', 'MANAGER')) OR " +
            "(g.contestCoordinatorType = 'LEADER' AND ur.role = 'OWNER'))")
    Set<Group> findManagedGroupsByUserId(@Param("userId") Long userId);
}
