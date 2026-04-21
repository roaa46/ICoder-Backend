package com.icoder.coding.editor.entity;

import com.icoder.user.management.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "code_templates")
public class CodeTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private String templateName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private Integer languageId;

    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String code;

    private boolean enabled;

    @NotNull
    private Instant createdAndUpdatedAt;
}
