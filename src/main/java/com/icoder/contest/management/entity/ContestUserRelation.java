package com.icoder.contest.management.entity;

import com.icoder.contest.management.enums.ContestRole;
import com.icoder.core.entity.BaseEntity;
import com.icoder.user.management.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "contest_user_relations")
public class ContestUserRelation extends BaseEntity<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contest_id")
    private Contest contest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ContestRole role;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "integer default 0")
    private Integer score = 0;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "integer default 0")
    private Integer penalty = 0;
}
