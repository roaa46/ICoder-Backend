package com.icoder.problem.management.entity;

import com.icoder.problem.management.enums.FormatType;
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
@Table(name = "contents")
public class SectionContent {
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
//    uncomment when scraping is done
//    @Lob
    private String content;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FormatType formatType;  // I think we don't need it, as the type will be Markdown

    @Column(nullable = false)
    private Integer orderIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private ProblemSection section;
}
