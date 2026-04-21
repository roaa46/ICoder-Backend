package com.icoder.group.management.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.icoder.group.management.enums.GroupRole;
import com.icoder.user.management.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "user_group_roles", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "group_id"})
})
public class UserGroupRole {
    @Id
    @GeneratedValue
    Long id;

    @JoinColumn(name = "user_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    User user;

    @JoinColumn(name = "group_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    Group group;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    GroupRole role;
}
