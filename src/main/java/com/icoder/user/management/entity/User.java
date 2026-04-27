package com.icoder.user.management.entity;

import com.icoder.activity.management.entity.ActivityLog;
import com.icoder.coding.editor.entity.CodeTemplate;
import com.icoder.group.management.entity.UserGroupRole;
import com.icoder.problem.management.entity.ProblemUserRelation;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
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

    private boolean verified = false;

    private Instant createdAt = Instant.now();

    private int acceptedCount = 0;

    private int attemptedCount = 0;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Token> tokens = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<UserGroupRole> groupRoles = new HashSet<>();

    private Instant lastVerificationEmailSentAt;

    @OneToMany(mappedBy = "user",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<ProblemUserRelation> problemUserRelations = new ArrayList<>();

    @OneToMany(mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private Set<CodeTemplate> templates = new HashSet<>();

    @OneToMany(mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<ActivityLog> activityLogs = new ArrayList<>();
}

