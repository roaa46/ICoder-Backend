package com.icoder.user.management.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Entity
@Data
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

    private boolean verified = false;

    private Instant createdAt = Instant.now();

    private int acceptedCount = 0;

    private int attemptedCount = 0;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Token> tokens;

    private Instant lastVerificationEmailSentAt;
}

