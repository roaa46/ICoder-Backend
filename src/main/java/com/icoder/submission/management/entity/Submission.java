package com.icoder.submission.management.entity;

import com.icoder.problem.management.entity.Problem;
import com.icoder.problem.management.enums.OJudgeType;
import com.icoder.submission.management.enums.SubmissionStatus;
import com.icoder.submission.management.enums.SubmissionVerdict;
import com.icoder.user.management.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "submissions")
public class Submission {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String submissionCode;

    @Enumerated(EnumType.STRING)
    private SubmissionVerdict verdict;

    @Enumerated(EnumType.STRING)
    private SubmissionStatus status;

    private boolean opened;

    private Integer timeUsage;
    private Integer memoryUsage;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OJudgeType onlineJudge;

    @Column(nullable = false)
    private String language;

    private String remoteRunId;
    private Instant submittedAt;
    private Instant updatedAt;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id")
    private Problem problem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bot_account_id")
    private BotAccount botAccount;

    // contest relation

    @PrePersist
    protected void onCreate() {
        submittedAt = Instant.now();
        if (status == null)
            status = SubmissionStatus.CREATED;
        if (verdict == null)
            verdict = SubmissionVerdict.PENDING;
    }
}
