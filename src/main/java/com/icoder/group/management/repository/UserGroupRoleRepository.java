package com.icoder.group.management.repository;

import com.icoder.group.management.entity.Group;
import com.icoder.group.management.entity.UserGroupRole;
import com.icoder.user.management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserGroupRoleRepository extends JpaRepository<UserGroupRole, Long> {
    Optional<UserGroupRole> findByUserAndGroup(User user, Group group);

    @Query("""
    SELECT COUNT(ugr) > 0
    FROM UserGroupRole ugr
    WHERE ugr.user.id = :userId
      AND ugr.group.id = :groupId
      AND ugr.role IN ('OWNER', 'MANAGER')
""")
    boolean isLeaderOfGroup(@Param("userId") Long userId,
                            @Param("groupId") Long groupId);

    @Query("""
       SELECT COUNT(ugr) > 0
       FROM UserGroupRole ugr
       WHERE ugr.user.id = :userId
         AND ugr.group.id = :groupId
       """)
    boolean existInGroup(@Param("userId") Long userId, @Param("groupId") Long groupId);
}
