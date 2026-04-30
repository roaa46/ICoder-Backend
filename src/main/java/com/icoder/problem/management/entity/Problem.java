package com.icoder.problem.management.entity;

import com.icoder.core.entity.BaseEntity;
import com.icoder.problem.management.enums.OJudgeType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
                columnNames = {"problemCode", "onlineJudge"}
        )
)
public class Problem extends BaseEntity<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private String problemCode;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OJudgeType onlineJudge;

    @Column(nullable = false)
    private String contestTitle;

    private String contestLink;

    @Column(nullable = false)
    private String problemTitle;

    @Column(nullable = false)
    private String problemLink;

    private long solvedCount = 0;

    private long attemptedCount = 0;

    private Instant fetchedAt;

    @OneToMany(mappedBy = "problem",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private Set<ProblemUserRelation> problemUserRelations = new HashSet<>();

    @OneToMany(mappedBy = "problem",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<ProblemSection> sections = new ArrayList<>();  // Problem Statement, Input, Output, Notes, Examples, Constraints, ...

    @OneToMany(mappedBy = "problem",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<ProblemProperty> properties = new ArrayList<>();

    public Problem(String problemCode, OJudgeType oJudgeType, String contestTitle, String contestLink, String problemTitle, String problemLink) {
        this.problemCode = problemCode;
        this.onlineJudge = oJudgeType;
        this.contestTitle = contestTitle;
        this.contestLink = contestLink;
        this.problemTitle = problemTitle;
        this.problemLink = problemLink;
    }
}
