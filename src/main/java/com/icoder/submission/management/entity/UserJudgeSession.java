package com.icoder.submission.management.entity;

import com.icoder.core.entity.BaseEntity;
import com.icoder.problem.management.enums.OJudgeType;
import com.icoder.user.management.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserJudgeSession extends BaseEntity<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private OJudgeType judgeType;

    @Column(columnDefinition = "TEXT")
    private String sessionData;

    private Instant lastUpdated;
}
