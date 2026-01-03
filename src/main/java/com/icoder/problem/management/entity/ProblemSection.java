package com.icoder.problem.management.entity;

import com.icoder.problem.management.enums.FormatType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Column(columnDefinition = "TEXT")
    @Lob
    private String content;

    @Enumerated(EnumType.STRING)
    private FormatType contentType;

    @OneToMany(mappedBy = "section",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<SectionContent> contents;
}
