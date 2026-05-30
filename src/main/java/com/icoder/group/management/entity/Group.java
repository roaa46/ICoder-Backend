package com.icoder.group.management.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.icoder.contest.management.entity.Contest;
import com.icoder.core.entity.BaseEntity;
import com.icoder.group.management.enums.ContestCoordinatorType;
import com.icoder.group.management.enums.Visibility;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Formula;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "groups")
public class Group extends BaseEntity<Long> {
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, updatable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private Boolean codeEnabled;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Visibility visibility;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContestCoordinatorType contestCoordinatorType;

    @Column(nullable = false)
    private String description;

    private String pictureUrl;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = true)
    private Long ownerId;

    @Formula("(SELECT COUNT(*) FROM user_group_roles ugr WHERE ugr.group_id = id)")
    private Long groupMembersCount;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<UserGroupRole> userRoles = new HashSet<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Contest> contests = new HashSet<>();
}
