package com.icoder.submission.management.entity;

import com.icoder.problem.management.enums.OJudgeType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "bot_accounts", indexes = {
        @Index(name = "idx_account_lookup", columnList = "judgeType, inUse, active, lastUsedAt")
})
public class BotAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OJudgeType judgeType;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(columnDefinition = "TEXT")
    private String cookies;

    @Builder.Default
    private boolean inUse = false;

    @Builder.Default
    private boolean active = true;

    @Builder.Default
    private Instant lastUsedAt = Instant.now();
}
