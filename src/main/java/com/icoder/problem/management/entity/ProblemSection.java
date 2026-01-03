package com.icoder.problem.management.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "sections")
public class ProblemSection {
    @Id
    @GeneratedValue
    private Long id;

    private String title;

    private int orderIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id")
    private Problem problem;

    @OneToMany(mappedBy = "section",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<SectionContent> contents = new ArrayList<>();
}
