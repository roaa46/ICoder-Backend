package com.icoder.user.management.entity;

import com.icoder.problem.management.entity.ProblemUserRelation;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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
    private List<Token> tokens;

    private String lastVerificationEmailSentAt;

    @OneToMany(mappedBy = "user",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<ProblemUserRelation> problemUserRelations = new ArrayList<>();
}
