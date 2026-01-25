package com.icoder.contest.management.entity;

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
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, updatable = false)
    private Instant beginTime;

    @Column(nullable = false)
    @DurationMax(days = 365, message = "Contest length cannot exceed 1 year")
    private Duration length;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ContestType contestType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ContestOpenness contestOpenness;

    private String description;

    private boolean historyRank;

    private String password;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;
}
