package com.icoder.problem.management.entity;

import com.icoder.core.enums.OJudgeType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "problems",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"problemCode", "oJudgeSource"}
        )
)
public class Problem {
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String problemCode;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OJudgeType onlineJudge;

    @Column(nullable = false)
    private String contestTitle;

    @Column(nullable = false)
    private String contestLink;

    @Column(nullable = false)
    private String problemTitle;

    @Column(nullable = false)
    private String problemLink;

    private long solvedCount = 0;

    private long attemptedCount = 0;

    private Instant fetchedAt = Instant.now();

    @OneToMany(mappedBy = "problem",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private Set<ProblemUserRelation> problemUserRelations = new HashSet<>();

    @OneToMany(mappedBy = "problem",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<ProblemSection> sections = new ArrayList<>();

    @OneToMany(mappedBy = "problem",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<ProblemProperty> properties = new ArrayList<>();
}
