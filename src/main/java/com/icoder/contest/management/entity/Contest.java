package com.icoder.contest.management.entity;

import com.icoder.contest.management.enums.ContestStatus;
import com.icoder.contest.management.enums.ContestType;
import com.icoder.contest.management.enums.ContestOpenness;
import com.icoder.group.management.entity.Group;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.time.DurationMax;

import java.time.Duration;
import java.time.Instant;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "contests")
public class Contest {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private Instant beginTime;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;
}
