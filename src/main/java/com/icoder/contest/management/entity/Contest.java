package com.icoder.contest.management.entity;

import com.icoder.contest.management.enums.ContestOpenness;
import com.icoder.contest.management.enums.ContestStatus;
import com.icoder.contest.management.enums.ContestType;
import com.icoder.core.entity.BaseEntity;
import com.icoder.group.management.entity.Group;
import com.icoder.submission.management.entity.Submission;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Formula;
import org.hibernate.validator.constraints.time.DurationMax;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "contests", indexes = {
        @Index(name = "idx_contest_status_start", columnList = "contestStatus, beginTime"),
        @Index(name = "idx_contest_status_end", columnList = "contestStatus, endTime")
})
public class Contest extends BaseEntity<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private Instant beginTime;

    @Column(nullable = false)
    private Instant endTime;

    @Column(nullable = false)
    @DurationMax(days = 365, message = "Contest length cannot exceed 1 year")
    private Duration length;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ContestType contestType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ContestOpenness contestOpenness; // private for group type, public & protected for classical type

    @Enumerated(EnumType.STRING)
    private ContestStatus contestStatus;

    private boolean historyRank; // display -> true, hide until the contest ends -> false

    private String password; // it will be set if openness is protected

    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    @OneToMany(mappedBy = "contest",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @Builder.Default
    private Set<ContestProblemRelation> problemRelation = new HashSet<>();

    @OneToMany(mappedBy = "contest",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @Builder.Default
    private Set<ContestUserRelation> userRelation = new HashSet<>();

    @OneToMany(mappedBy = "contest",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @Builder.Default
    private Set<Submission> submissions = new HashSet<>();

    @Formula("""
            (
               SELECT COALESCE(COUNT(*), 0)
               FROM contest_user_relations cur
               WHERE cur.contest_id = id
                 AND cur.role = 'PARTICIPANT'
            )
            """)
    private Long participantsCount;

    public void addUserRelation(ContestUserRelation relation) {
        if (this.userRelation == null) {
            this.userRelation = new HashSet<>();
        }
        this.userRelation.add(relation);
        relation.setContest(this);
    }
}
