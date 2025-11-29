package com.icoder.problem.management.entity;

import com.icoder.user.management.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "problem_user_relations",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"user_id", "problem_id"}
        ))
public class ProblemUserRelation {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id")
    private Problem problem;

    private boolean isFavourite;
    private boolean isSolved;
    private boolean isAttempted;
}
