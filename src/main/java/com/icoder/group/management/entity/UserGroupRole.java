package com.icoder.group.management.entity;

import com.icoder.core.enums.GroupRole;
import com.icoder.user.management.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Table(name = "user_group_roles", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "group_id"})
})
public class UserGroupRole {
    @Id
    @GeneratedValue
    Long id;

    @JoinColumn(name = "user_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    User user;

    @JoinColumn(name = "group_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    Group group;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    GroupRole role;
}
