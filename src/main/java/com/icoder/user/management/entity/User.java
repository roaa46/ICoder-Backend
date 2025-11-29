package com.icoder.user.management.entity;

import com.icoder.group.management.entity.Group;
import com.icoder.group.management.entity.UserGroupRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String handle;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nickname;

    private String school;

    @Column(nullable = false)
    private String email;

    private String pictureUrl;

    @Column(nullable = false)
    private boolean verified = false;

    @Column(nullable = false)
    private String createdAt = Instant.now().toString();

    @Column(nullable = false)
    private int acceptedCount = 0;

    @Column(nullable = false)
    private int attemptedCount = 0;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Token> tokens = new ArrayList<>();

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Group> ownedGroups = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<UserGroupRole> groupRoles = new HashSet<>();

    private String lastVerificationEmailSentAt;
}

