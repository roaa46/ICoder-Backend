package com.icoder.problem.management.entity;

import com.icoder.core.entity.BaseEntity;
import com.icoder.user.management.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "problem_user_relations",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"user_id", "problem_id"}
        ))
public class ProblemUserRelation extends BaseEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id")
    private Problem problem;

    @Column(name = "is_favorite")
    private boolean favorite;
    @Column(name = "is_solved")
    private boolean solved;
    @Column(name = "is_attempted")
    private boolean attempted;
}
