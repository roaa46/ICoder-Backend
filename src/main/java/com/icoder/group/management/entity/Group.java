package com.icoder.group.management.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.icoder.contest.management.entity.Contest;
import com.icoder.group.management.enums.ContestCoordinatorType;
import com.icoder.group.management.enums.Visibility;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "groups")
public class Group {
    @Id
    @GeneratedValue
    private long id;

    @Column(nullable = false, updatable = false, unique = true)
    private String code;
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

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<UserGroupRole> userRoles = new HashSet<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Contest> contests = new HashSet<>();
}
