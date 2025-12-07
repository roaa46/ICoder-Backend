package com.icoder.group.management.repository;

import com.icoder.group.management.entity.Group;
import com.icoder.group.management.enums.Visibility;
import com.icoder.user.management.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface GroupRepository extends JpaRepository<Group, Long> {
    boolean existsByCode(String code);

    @Query("SELECT g FROM Group g JOIN UserGroupRole ugr ON g.id = ugr.group.id JOIN User u ON ugr.user.id = u.id WHERE u.handle = :handle")
    Page<Group> getMyGroups(@Param("handle") String handle, Pageable pageable);

    @Query("SELECT g FROM Group g WHERE g.visibility = :visibility")
    Page<Group> getAllPublicGroups(@Param("visibility") Visibility visibility, Pageable pageable);

    @Query("SELECT u FROM User u JOIN UserGroupRole ugr ON u.id = ugr.user.id WHERE ugr.group.id = :groupId AND ugr.role IN ('OWNER', 'ADMIN')")
    Set<User> getLeaders(@Param("groupId") Long groupId);

    @Query("SELECT CASE WHEN COUNT(ugr) > 0 THEN true ELSE false END " +
            "FROM UserGroupRole ugr " +
            "WHERE ugr.user.id = :userId AND ugr.group.id = :groupId")
    boolean existInGroup(@Param("userId") Long userId, @Param("groupId") Long groupId);
}
