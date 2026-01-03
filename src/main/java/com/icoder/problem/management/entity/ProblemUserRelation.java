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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "problem_id")
    private Problem problem;

    @Column(name = "is_favorite")
    private boolean favorite;
    @Column(name = "is_solved")
    private boolean solved;
    @Column(name = "is_attempted")
    private boolean attempted;
}
